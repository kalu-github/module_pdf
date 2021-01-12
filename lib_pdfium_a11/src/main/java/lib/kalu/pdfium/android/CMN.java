package lib.kalu.pdfium.android;

import android.view.View;
import android.view.ViewGroup;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;


//common
public class CMN {
	public static long stst;
	public static long ststrt;
	public static long stst_add;
	public static boolean testing;
	
	public static void rt(Object... o) {
		ststrt = System.currentTimeMillis();
	}
	public static void pt(Object...args) {
		CMN.Log(Arrays.toString(args)+" "+(System.currentTimeMillis()-ststrt));
	}
	
	///*[!0] Start debug flags and methods
	public static boolean testFLoatSearch;
	public static boolean editAll;
	public static boolean darkRequest=true;
	public static void Log(Object... o) {
		StringBuilder msg= new StringBuilder();
		if(o!=null)
			for (Object o1 : o) {
				if(o1!=null) {
					if (o1 instanceof Exception) {
						ByteArrayOutputStream s = new ByteArrayOutputStream();
						PrintStream p = new PrintStream(s);
						((Exception) o1).printStackTrace(p);
						msg.append(s.toString());
					}
					if (o1 instanceof Exception) {
						ByteArrayOutputStream s = new ByteArrayOutputStream();
						PrintStream p = new PrintStream(s);
						((Exception) o1).printStackTrace(p);
						msg.append(s.toString());
					}
					if (o1 instanceof int[]) {
						msg.append(Arrays.toString((int[]) o1));
						continue;
					} else if (o1 instanceof String[]) {
						msg.append(Arrays.toString((Object[]) o1));
						continue;
					} else if (o1 instanceof short[]) {
						msg.append(Arrays.toString((short[]) o1));
						continue;
					} else if (o1 instanceof byte[]) {
						msg.append(Arrays.toString((byte[]) o1));
						continue;
					}
				}
				if(msg.length()>0) msg.append(", ");
				msg.append(o1);
			}
		if(testing) {
			System.out.println(msg.toString());
		} else {
			android.util.Log.d("fatal poison",msg.toString());
		}
	}
	public static void recurseLog(View v,String... depths) {
		String depth = depths!=null && depths.length>0?depths[0]:"- ";
		String depth_plus_1=depth+"- ";
		if(!ViewGroup.class.isInstance(v)) return;
		ViewGroup vg = (ViewGroup) v;
		for(int i=0;i<vg.getChildCount();i++) {
			View CI = vg.getChildAt(i);
			Log(depth+CI+" == "+Integer.toHexString(CI.getId())+"/"+CI.getBackground()+"/"+CI.getAnimation()+"/"+CI.getAlpha());
			if(ViewGroup.class.isInstance(CI))
				recurseLog(CI,depth_plus_1);
		}
	}
	public static void recurseLogCascade(View now) {
		if(now==null) return;
		while(now.getParent()!=null) {
			if(!View.class.isInstance(now.getParent())) {
				Log("-!-reached none view object or null : "+now.getParent());
				break;
			}
			now=(View) now.getParent();
		}
		Log("Cascade Start Is : "+now+" == "+Integer.toHexString(now.getId())+"/"+now.getBackground()+"/"+now.getAnimation()+"/"+now.getAlpha());
		recurseLog(now);
		//now.setBackgroundResource(R.drawable.popup_shadow);
	}
	
	public static int id(Object obj) {
		return System.identityHashCode(obj);
	}
	//[!1] End debug flags and methods*/
	
	
	static long lastTimeRecorded;
	
	public static void recordTime() {
		lastTimeRecorded=System.currentTimeMillis();
	}
	
	public static long timeElapsed() {
		return System.currentTimeMillis()-lastTimeRecorded;
	}
	
	public static long now() {
		return System.currentTimeMillis();
	}
	
	public static Object tid() {
		return Thread.currentThread().getId();
	}
}