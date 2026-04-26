package com.example.protypebillingsystem;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.protypebillingsystem.api.PaymentApi;
import com.example.protypebillingsystem.models.PaymentRequest;
import com.example.protypebillingsystem.models.PaymentResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentsFragment extends Fragment {

    private LoadingDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payments, container, false);
        
        loadingDialog = new LoadingDialog(requireContext());

        view.findViewById(R.id.pay_mobile_money).setOnClickListener(v -> confirmPayment("Mobile Money"));

        view.findViewById(R.id.pay_card).setOnClickListener(v -> confirmPayment("Mastercard"));

        view.findViewById(R.id.pay_cash).setOnClickListener(v -> showManualCashierDialog());

        return view;
    }

    private void confirmPayment(String method) {
        PatientSession s = PatientSession.getInstance();
        if (s.netPayable <= 0) {
            Toast.makeText(getContext(), "No pending bills", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(getContext())
            .setTitle("Confirm Payment")
            .setMessage("Do you want to initiate " + method + " payment for $" + s.netPayable + "?")
            .setPositiveButton("Yes", (dialog, which) -> initiatePayment(method, s.netPayable))
            .setNegativeButton("No", null)
            .show();
    }

    private void initiatePayment(String method, double amount) {
        if (!NetworkUtils.isConnected(requireContext())) {
            Toast.makeText(getContext(), "Internet connection required for online payment", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.show();
        PaymentApi paymentApi = ApiClient.getClient(requireContext()).create(PaymentApi.class);
        paymentApi.initiatePayment(new PaymentRequest(method, amount)).enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    PaymentResponse payResponse = response.body();
                    // Navigate to Receipt
                    ReceiptFragment receiptFragment = new ReceiptFragment();
                    Bundle args = new Bundle();
                    args.putString("receiptId", payResponse.getReceiptId());
                    args.putString("billId", PatientSession.getInstance().billId);
                    receiptFragment.setArguments(args);
                    
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).loadFragmentInstance(receiptFragment);
                    }
                } else {
                    showError("Payment failed. Please proceed to cashier.");
                }
            }

            @Override
            public void onFailure(Call<PaymentResponse> call, Throwable t) {
                loadingDialog.dismiss();
                showError("Network error: " + t.getMessage() + ". Please proceed to cashier.");
            }
        });
    }

    private void showError(String message) {
        new AlertDialog.Builder(getContext())
            .setTitle("Payment Error")
            .setMessage(message)
            .setPositiveButton("Go to Cashier", (dialog, which) -> showManualCashierDialog())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showManualCashierDialog() {
        PatientSession s = PatientSession.getInstance();
        new AlertDialog.Builder(getContext())
            .setTitle("Manual Payment")
            .setMessage("Please proceed to the cashier and present Bill ID: " + s.billId)
            .setPositiveButton("OK", null)
            .show();
    }
}
