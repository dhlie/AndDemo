package dhl.anddemo.matrix;

import android.graphics.Matrix;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import dhl.anddemo.R;
import dhl.anddemo.base.BaseActivity;
import dhl.anddemo.base.TitleBar;

/**
 * Created by DuanHl on 2016/3/15.
 */
public class MatrixEditActivity extends BaseActivity {

    private EditText mET11, mET12, mET13, mET21, mET22, mET23, mET31, mET32, mET33;
    private ImageView mIVResult;

    private float m11, m12, m13, m21, m22, m23, m31, m32, m33;
    private Matrix mMatrix = new Matrix();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acti_matrix_edit);

        TitleBar.SimpleTitleBarClickListener titleBarClickListener = new TitleBar.SimpleTitleBarClickListener() {
            @Override
            public void onLeftClick(View v) {
                finish();
            }

            @Override
            public void onRightFirstClick(View v) {
                reset();
            }
        };
        TitleBar titleBar = (TitleBar) findViewById(R.id.titlebar);
        titleBar.setTitleBarClickListener(titleBarClickListener);
        titleBar.setTitle(getClass().getSimpleName());

        mIVResult = (ImageView) findViewById(R.id.iv_image);

        mET11 = (EditText) findViewById(R.id.et11);
        mET12 = (EditText) findViewById(R.id.et12);
        mET13 = (EditText) findViewById(R.id.et13);
        mET21 = (EditText) findViewById(R.id.et21);
        mET22 = (EditText) findViewById(R.id.et22);
        mET23 = (EditText) findViewById(R.id.et23);
        mET31 = (EditText) findViewById(R.id.et31);
        mET32 = (EditText) findViewById(R.id.et32);
        mET33 = (EditText) findViewById(R.id.et33);

        mET11.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    m11 = 1f;
                    return;
                }
                m11 = Float.parseFloat(s.toString());
            }
        });
        mET12.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    m12 = 0f;
                    return;
                }
                m12 = Float.parseFloat(s.toString());
            }
        });
        mET13.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    m13 = 0f;
                    return;
                }
                m13 = Float.parseFloat(s.toString());
            }
        });
        mET21.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    m21 = 0f;
                    return;
                }
                m21 = Float.parseFloat(s.toString());
            }
        });
        mET22.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    m22 = 1f;
                    return;
                }
                m22 = Float.parseFloat(s.toString());
            }
        });
        mET23.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    m23 = 0f;
                    return;
                }
                m23 = Float.parseFloat(s.toString());
            }
        });
        mET31.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    m31 = 0f;
                    return;
                }
                m31 = Float.parseFloat(s.toString());
            }
        });
        mET32.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    m32 = 0f;
                    return;
                }
                m32 = Float.parseFloat(s.toString());
            }
        });
        mET33.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    m33 = 1f;
                    return;
                }
                m33 = Float.parseFloat(s.toString());
            }
        });

        findViewById(R.id.btn_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });

        findViewById(R.id.btn_apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apply();
            }
        });

        reset();
    }

    private void reset() {
        m11 = m12 = m13 = m21 = m22 = m23 = m31 = m32 = m33 = 0;
        m11 = m22 = m33 = 1f;

        mET11.setText(String.valueOf(m11));
        mET12.setText(String.valueOf(m12));
        mET13.setText(String.valueOf(m13));
        mET21.setText(String.valueOf(m21));
        mET22.setText(String.valueOf(m22));
        mET23.setText(String.valueOf(m23));
        mET31.setText(String.valueOf(m31));
        mET32.setText(String.valueOf(m32));
        mET33.setText(String.valueOf(m33));

        apply();
    }

    private void apply() {
        float[] farr = new float[] {
                m11, m12, m13,
                m21, m22, m23,
                m31, m32, m33
        };
        mMatrix.setValues(farr);

        mIVResult.setImageMatrix(mMatrix);
    }
}
