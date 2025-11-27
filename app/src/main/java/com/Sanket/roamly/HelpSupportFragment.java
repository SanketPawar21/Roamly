package com.Sanket.roamly;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class HelpSupportFragment extends Fragment {

    private static final String SUPPORT_EMAIL = "support@roamly.app"; // change to your real email
    private static final String SUPPORT_PHONE = "+911234567890";      // change to your real phone

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help_support, container, false);

        MaterialButton btnEmail = view.findViewById(R.id.btnEmailSupport);
        MaterialButton btnCall = view.findViewById(R.id.btnCallSupport);
        MaterialButton btnFaq = view.findViewById(R.id.btnOpenFaq);

        btnEmail.setOnClickListener(v -> openEmail());
        btnCall.setOnClickListener(v -> callSupport());
        btnFaq.setOnClickListener(v -> openFaqPage());

        return view;
    }

    private void openEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + SUPPORT_EMAIL));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Roamly - Help & Support");
        startActivity(Intent.createChooser(intent, "Send email"));
    }

    private void callSupport() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + SUPPORT_PHONE));
        startActivity(intent);
    }

    private void openFaqPage() {
        // You can change this to your own hosted FAQ page later
        String url = "https://www.google.com/search?q=roamly+faq";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
