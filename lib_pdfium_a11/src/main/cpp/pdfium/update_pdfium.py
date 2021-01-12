#!/usr/bin/python

import urllib2
import os
import sys
from subprocess import call

CHROMIUM_VERSION_TRACKING_URL = "https://omahaproxy.appspot.com/all"
CHROMIUM_BUILD_TYPE = "stable"
CHROMIUM_OS = "android"

CHROMIUM_SOURCE_URL = "https://chromium.googlesource.com/chromium/src/+/refs/tags"
CHROMIUM_DEPS_FILE = "DEPS"

PDFIUM_GIT_REPO = "https://pdfium.googlesource.com/pdfium.git"

MAKE_FILES = ["Android.bp",
              "third_party/pdfiumopenjpeg.bp",
              "third_party/pdfiumlcms.bp",
              "third_party/pdfiumjpeg.bp",
              "third_party/pdfiumagg23.bp",
              "third_party/pdfiumzlib.bp",
              "third_party/pdfiumbigint.bp",
              "third_party/Android.bp",
              "pdfiumfpdftext.bp",
              "pdfiumfpdfdoc.bp",
              "pdfiumfdrm.bp",
              "pdfiumfxcodec.bp",
              "pdfiumfpdfapi.bp",
              "pdfiumfxcrt.bp",
              "pdfiumfxge.bp",
              "pdfiumjavascript.bp",
              "pdfiumformfiller.bp",
              "pdfiumfxedit.bp",
              "pdfiumpdfwindow.bp",
              "pdfium.bp"]

OWNERS_FILES = ["OWNERS", "docs/OWNERS", "third_party/base/numerics/OWNERS"]

COPY_FILES = [os.path.basename(__file__), ".git", "MODULE_LICENSE_BSD", "NOTICE"] + MAKE_FILES
REMOVE_FILES = [os.path.basename(__file__), ".git", ".gitignore"] + OWNERS_FILES

def getStableChromiumVersion():
   """ :return the latest chromium version """

   chromiumVersions = urllib2.urlopen(CHROMIUM_VERSION_TRACKING_URL)

   for chromiumVersionStr in chromiumVersions.read().split("\n"):
       chromiumVersion = chromiumVersionStr.split(",")

       if chromiumVersion[0] == CHROMIUM_OS and chromiumVersion[1] == CHROMIUM_BUILD_TYPE:
           return chromiumVersion[2]

   raise Exception("Could not find latest %s chromium version for %s at %s"
                   % (CHROMIUM_BUILD_TYPE, CHROMIUM_OS, CHROMIUM_VERSION_TRACKING_URL))


def getPdfiumRevision():
    """ :return the pdfium version used by the latest chromium version """

    try:
        deps = urllib2.urlopen("%s/%s/%s" % (CHROMIUM_SOURCE_URL, getStableChromiumVersion(),
                                             CHROMIUM_DEPS_FILE))

        # I seem to not be able to get the raw file, hence grep the html file
        return deps.read().split("pdfium_revision&")[1].split("&#39;")[1]
    except Exception as e:
        raise Exception("Could not extract pdfium revision from %s/%s/%s: %s"
                       % (CHROMIUM_SOURCE_URL, getStableChromiumVersion(), CHROMIUM_DEPS_FILE, e))


def downloadPdfium(newDir, rev):
    """ Download the newest version of pdfium to the new directory

    :param newDir: The new files
    :param rev: The revision to change to
    """

    call(["git", "clone", PDFIUM_GIT_REPO, newDir])
    os.chdir(newDir)
    call(["git", "reset", "--hard", rev])


def removeFiles(newDir):
    """ Remove files that should not be checked in from the original download

    :param newDir: The new files
    """

    for fileName in REMOVE_FILES:
        call(["rm", "-rf", os.path.join(newDir, fileName)])


def copyFiles(currentDir, newDir):
    """ Copy files needed to make pdfium work with android

    :param currentDir: The current files
    :param newDir: The new files
    """

    for fileName in COPY_FILES:
        call(["cp", "-r", os.path.join(currentDir, fileName), os.path.join(newDir, fileName)])


def exchange(currentDir, newDir, oldDir):
    """ Update current to new and save current in old.

    :param currentDir: The current files
    :param newDir: The new files
    :param oldDir: The old files
    """

    call(["mv", currentDir, oldDir])
    call(["mv", newDir, currentDir])


if __name__ == "__main__":
   rev = getPdfiumRevision()
   targetDir = os.path.dirname(os.path.realpath(__file__))
   newDir = targetDir + ".new"
   oldDir = targetDir + ".old"

   try:
       downloadPdfium(newDir, rev)
       removeFiles(newDir)
       copyFiles(targetDir, newDir)
       exchange(targetDir, newDir, oldDir)
       print("Updated pdfium to " + rev + " (Chrome " + getStableChromiumVersion() + "). Old files "
             "are in " + oldDir + ". Please verify if build files need to be updated.")

       sys.exit(0)
   except:
       call(["rm", "-rf", newDir])
       sys.exit(1)

