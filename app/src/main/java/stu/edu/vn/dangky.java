package stu.edu.vn;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class dangky extends AppCompatActivity {

    TextView TonTai;
    EditText txtEmail, txtpass,txtpassconfirm;
    Button btndangky;
    FirebaseAuth mAuth;

    public void addControls(){
        txtEmail =findViewById(R.id.txtEmail);
        txtpass =findViewById(R.id.txtPassword);
        txtpassconfirm=findViewById(R.id.txtPasswordConfirm);
        btndangky =findViewById(R.id.btnDangKy);
        mAuth =  FirebaseAuth.getInstance();
        TonTai =findViewById(R.id.txtAccount);
        String text = "Bạn đã có tài khoản?";
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new UnderlineSpan(), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        TonTai.setText(spannableString);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dangky);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addControls();
        TonTai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(dangky.this,Dangnhap.class);
                startActivity(intent);
            }
        });
        btndangky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dang_ky_user();
            }
        });
    }

    private void dang_ky_user() {
        String email = txtEmail.getText().toString();
        String pass =txtpass.getText().toString();
        String passconfirm= txtpassconfirm.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(pass)){
            Toast.makeText(this, "Vui lòng nhập password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(passconfirm)) {
            Toast.makeText(this, "Vui lòng nhập lại password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pass.equals(passconfirm)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(dangky.this, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(dangky.this,MainActivity.class);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(dangky.this, "Tạo tài khoản không thành công", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}