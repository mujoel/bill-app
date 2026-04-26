package com.example.protypebillingsystem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.protypebillingsystem.api.PrescriptionApi;
import com.example.protypebillingsystem.models.PrescriptionResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrescriptionFragment extends Fragment {

    private LinearLayout llContent;
    private TextView tvMedicationName, tvDosage, tvDate, tvMessage;
    private LoadingDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_prescription, container, false);

        llContent = view.findViewById(R.id.ll_prescription_content);
        tvMedicationName = view.findViewById(R.id.tv_medication_name);
        tvDosage = view.findViewById(R.id.tv_dosage);
        tvDate = view.findViewById(R.id.tv_prescription_date);
        tvMessage = view.findViewById(R.id.tv_prescription_message);
        
        loadingDialog = new LoadingDialog(requireContext());

        fetchPrescription();

        return view;
    }

    private void fetchPrescription() {
        PatientSession s = PatientSession.getInstance();
        if (s.patientId == null || s.patientId.isEmpty()) return;

        if (!NetworkUtils.isConnected(requireContext())) {
            Toast.makeText(getContext(), "Internet connection required to load prescription", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.show();
        PrescriptionApi api = ApiClient.getClient(requireContext()).create(PrescriptionApi.class);
        api.getPrescription(s.patientId).enqueue(new Callback<PrescriptionResponse>() {
            @Override
            public void onResponse(Call<PrescriptionResponse> call, Response<PrescriptionResponse> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    PrescriptionResponse presResponse = response.body();
                    if (presResponse.isPaymentConfirmed()) {
                        tvMessage.setVisibility(View.GONE);
                        llContent.setVisibility(View.VISIBLE);
                        
                        tvMedicationName.setText("Medication: " + presResponse.getMedicationName());
                        tvDosage.setText("Dosage: " + presResponse.getDosage());
                        tvDate.setText("Date: " + presResponse.getPrescriptionDate());
                    } else {
                        tvMessage.setVisibility(View.VISIBLE);
                        llContent.setVisibility(View.GONE);
                    }
                } else {
                    tvMessage.setText("Complete payment to access your prescription");
                    tvMessage.setVisibility(View.VISIBLE);
                    llContent.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<PrescriptionResponse> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(getContext(), "Network error: failed to load prescription", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
