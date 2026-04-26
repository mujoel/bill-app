package com.example.protypebillingsystem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.protypebillingsystem.api.PaymentApi;
import com.example.protypebillingsystem.models.PaymentResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReceiptFragment extends Fragment {

    private String receiptId;
    private String billId;
    private TextView tvReceiptId, tvAmountPaid, tvPaymentDate, tvPaymentMethod;
    private LoadingDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receipt, container, false);

        if (getArguments() != null) {
            receiptId = getArguments().getString("receiptId");
            billId = getArguments().getString("billId");
        }

        tvReceiptId = view.findViewById(R.id.tv_receipt_id);
        tvAmountPaid = view.findViewById(R.id.tv_amount_paid);
        tvPaymentDate = view.findViewById(R.id.tv_payment_date);
        tvPaymentMethod = view.findViewById(R.id.tv_payment_method);
        loadingDialog = new LoadingDialog(requireContext());

        view.findViewById(R.id.btn_download_receipt).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Downloading receipt...", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_back_home).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(0); // Go to Home
            }
        });

        if (billId != null) {
            fetchReceiptDetails();
        }

        return view;
    }

    private void fetchReceiptDetails() {
        if (!NetworkUtils.isConnected(requireContext())) {
            Toast.makeText(getContext(), "No internet connection to fetch receipt details", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.show();
        PaymentApi paymentApi = ApiClient.getClient(requireContext()).create(PaymentApi.class);
        paymentApi.getPaymentStatus(billId).enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    PaymentResponse payResponse = response.body();
                    tvReceiptId.setText("Receipt ID: " + payResponse.getReceiptId());
                    tvAmountPaid.setText("Amount Paid: $" + payResponse.getAmountPaid());
                    tvPaymentDate.setText("Date: " + payResponse.getDate());
                    tvPaymentMethod.setText("Method: " + payResponse.getPaymentMethod());
                } else {
                    Toast.makeText(getContext(), "Failed to load receipt details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PaymentResponse> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(getContext(), "Network error while loading receipt", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
