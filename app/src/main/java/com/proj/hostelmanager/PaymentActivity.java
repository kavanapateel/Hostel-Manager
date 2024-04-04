package com.proj.hostelmanager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {

    private static final long REDIRECT_DELAY = 3000; // Delay in milliseconds

    private TextView paymentTitle;
    private final String paymentText = "Payment processing...";
    private int currentCharIndex = 0;

    DatabaseConnect databaseConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        paymentTitle = findViewById(R.id.text_payment_title);
        databaseConnect = new DatabaseConnect(getApplicationContext());

        // Start typing effect
        startTypingEffect();

        // Simulate payment processing by adding a delay
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            String id = getIntent().getStringExtra("id");
            if(id != null) {
                databaseConnect.updateFeePayments(id);
            }

            // Redirect to MainActivity
            Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
            intent.putExtra("name", getIntent().getStringExtra("name"));
            intent.putExtra("id", id);
            startActivity(intent);
            finish();
        }, REDIRECT_DELAY);
    }

    private void startTypingEffect() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (currentCharIndex <= paymentText.length()) {
                String displayedText = paymentText.substring(0, currentCharIndex);
                paymentTitle.setText(displayedText);
                currentCharIndex++;
                startTypingEffect();
            }
        }, 100); // Delay between each character, adjust as needed
    }
}
