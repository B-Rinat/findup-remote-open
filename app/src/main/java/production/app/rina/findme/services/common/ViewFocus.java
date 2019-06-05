package production.app.rina.findme.services.common;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import production.app.rina.findme.R;

public class ViewFocus {

    private Activity activity;

    private int previousFocus = 0;

    public ViewFocus(Activity activity) {
        this.activity = activity;
    }

    public boolean isViewNullOrEmpty(View v, String showDefault) {
        if (v == null
                || ((EditText) v).getText().toString().isEmpty()) {
            Toast.makeText(activity, showDefault, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return false;
        }
    }

    public void setOnClickHelper(final View[] tv) {
        for (View v : tv) {
            requestFocus(v);
        }
        tv[0].requestFocus();
    }

    public void setOnClickHelper(final View tv) {
        requestFocus(tv);
    }

    private void requestFocus(final View tv) {
        tv.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (previousFocus != tv.getId()) {
                        tv.setBackgroundResource(R.drawable.input_text_style_active);
                        if (previousFocus != 0) {
                            View et = activity.findViewById(previousFocus);
                            if (et != null) {
                                et.setBackgroundResource(R.drawable.input_text_style);
                            }
                        }
                    }
                    previousFocus = tv.getId();
                }
            }
        });
    }
}
