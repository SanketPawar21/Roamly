package com.Sanket.roamly;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PrivacyPolicyFragment extends Fragment {

    public PrivacyPolicyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_privacy_policy, container, false);

        TextView content = view.findViewById(R.id.privacy_content);

        content.setText(getPrivacyText());

        return view;
    }

    private String getPrivacyText() {

        return "Roamly - Privacy Policy\n\n" +
                "Last Updated: November 2025\n\n" +
                "Roamly is committed to protecting your personal information and ensuring a safe travelling experience. " +
                "This Privacy Policy explains how we collect, store, use, and safeguard your data.\n\n\n" +

                "1. Information We Collect\n" +
                "- Name, phone number, email\n" +
                "- Profile details such as age, gender, interests\n" +
                "- Location information (Only when you share for emergency or trips)\n" +
                "- Device information for security and analytics\n" +
                "- Trip details, participants, preferences\n\n\n" +

                "2. How We Use Your Information\n" +
                "- To create and manage your account\n" +
                "- To show nearby trips and matches\n" +
                "- To ensure safety by identity verification\n" +
                "- To allow emergency sharing with added contacts\n" +
                "- To improve app features and user support\n\n\n" +

                "3. Location Access\n" +
                "Roamly uses your location only for:\n" +
                "- Trip suggestions\n" +
                "- Emergency live location sharing\n" +
                "- Navigation features\n" +
                "We do NOT track your location in the background without permission.\n\n\n" +

                "4. Sharing Your Information\n" +
                "We NEVER sell or rent your data.\n" +
                "We may share limited information only with:\n" +
                "- Emergency contacts (when you press emergency button)\n" +
                "- Law authorities (only upon legal requirement)\n\n\n" +

                "5. Safety of Female Users\n" +
                "- Additional emergency contacts\n" +
                "- Quick SOS button\n" +
                "- Auto-sharing live location with trusted contacts\n\n\n" +

                "6. Data Security\n" +
                "We use encryption and advanced security practices to protect your data. " +
                "Your information is stored safely using Firebase services.\n\n\n" +

                "7. Deleting Your Data\n" +
                "You can request data deletion at any time by contacting our support.\n\n\n" +

                "8. Children’s Privacy\n" +
                "Roamly does not allow users under the age of 13.\n\n\n" +

                "9. Changes To This Policy\n" +
                "We may update this policy occasionally. Major changes will be notified in the app.\n\n\n" +

                "10. Contact Us\n" +
                "If you have questions or concerns, contact us at:\n" +
                "support@roamlyapp.com\n";

    }
}
