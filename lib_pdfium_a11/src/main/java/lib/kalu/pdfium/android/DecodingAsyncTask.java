/**
 * Copyright 2016 Bartosz Schiller
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lib.kalu.pdfium.android;

import android.os.AsyncTask;

import lib.kalu.pdfium.android.source.DocumentSource;
import lib.kalu.pdfium.PdfDocument;
import lib.kalu.pdfium.PdfiumCore;
import lib.kalu.pdfium.util.Size;

import java.lang.ref.WeakReference;

class DecodingAsyncTask extends AsyncTask<Void, Void, Throwable> {

    private boolean cancelled;

    private WeakReference<PDocView> pdfViewReference;

    private PdfiumCore pdfiumCore;
    private String password;
    private DocumentSource docSource;
    private PdfFile pdfFile;

    DecodingAsyncTask(DocumentSource docSource, String password, PDocView pDocView, PdfiumCore pdfiumCore) {
        this.docSource = docSource;
        this.cancelled = false;
        this.pdfViewReference = new WeakReference<>(pDocView);
        this.password = password;
        this.pdfiumCore = pdfiumCore;
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        try {
            PDocView pDocView = pdfViewReference.get();
            if (pDocView != null) {
                PdfDocument pdfDocument = docSource.createDocument(pDocView.getContext(), pdfiumCore, password);
                pdfFile = new PdfFile(pdfiumCore, pdfDocument, pDocView.getPageFitPolicy(), getViewSize(pDocView),
						pDocView.isSwipeVertical(), pDocView.getSpacingPx(), pDocView.isAutoSpacingEnabled(),
                        pDocView.isFitEachPage());
                return null;
            } else {
                return new NullPointerException("pdfView == null");
            }

        } catch (Throwable t) {
            return t;
        }
    }

    private Size getViewSize(PDocView pDocView) {
        return new Size(pDocView.getWidth(), pDocView.getHeight());
    }

    @Override
    protected void onPostExecute(Throwable t) {
        PDocView pDocView = pdfViewReference.get();
        if (pDocView != null) {
            if (t != null) {
                pDocView.loadError(t);
                return;
            }
            if (!cancelled) {
                pDocView.loadComplete(pdfFile);
            }
        }
    }

    @Override
    protected void onCancelled() {
        cancelled = true;
    }
}
