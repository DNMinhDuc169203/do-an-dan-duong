package stu.edu.vn;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    private EditText txtEmail, txtPassword;
    private Button btnDangKy;
    private FirebaseAuth mAuth;

    public void addControls(){
    txtEmail = findViewById(R.id.txtEmail);
    txtPassword = findViewById(R.id.txtPassword);
    btnDangKy = findViewById(R.id.btnDangKy);
    mAuth = FirebaseAuth.getInstance();
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

        btnDangKy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = txtEmail.getText().toString().trim();
                String password = txtPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                    Toast.makeText(dangky.this,"Vui long nhap thong tin day du", Toast.LENGTH_SHORT).show();
                }
                else {
                    dang_Ky_user(email,password);
                }
            }
        });
    }

    private void dang_Ky_user(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(dangky.this, "Dang Ky thanh cong", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(dangky.this, "Dang ky that bai" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}