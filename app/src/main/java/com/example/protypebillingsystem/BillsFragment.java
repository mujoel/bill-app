package com.example.protypebillingsystem;

import android.app.Dialog;
import android.database.Cursor;
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

import com.example.protypebillingsystem.api.BillingApi;
import com.example.protypebillingsystem.models.BillResponse;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BillsFragment extends Fragment {

    private LinearLayout billContainer;
    private TextView tvTotal, tvNetTop, tvSubtotal, tvInsuranceLabel, tvInsuranceAmount, tvBillTotal, tvEmptyState;
    private LoadingDialog loadingDialog;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bills, container, false);
        
        tvTotal = view.findViewById(R.id.tv_total_charges);
        tvNetTop = view.findViewById(R.id.tv_net_payable_top);
        billContainer = view.findViewById(R.id.bill_items_container);
        tvSubtotal = view.findViewById(R.id.tv_subtotal);
        tvInsuranceLabel = view.findViewById(R.id.tv_insurance_label);
        tvInsuranceAmount = view.findViewById(R.id.tv_insurance_amount);
        tvBillTotal = view.findViewById(R.id.tv_bill_grand_total);
        
        // Let's assume tvEmptyState doesn't exist in layout yet, we can create it dynamically or tolerate null
        // Actually, I'll programmatically create empty state if needed.
        
        loadingDialog = new LoadingDialog(requireContext());
        dbHelper = new DatabaseHelper(requireContext());

        view.findViewById(R.id.btn_show_qr).setOnClickListener(v -> showQrDialog());

        fetchBill();

        return view;
    }

    private void fetchBill() {
        PatientSession s = PatientSession.getInstance();
        if (s.patientId == null || s.patientId.isEmpty()) return;

        if (NetworkUtils.isConnected(requireContext())) {
            loadingDialog.show();
            BillingApi billingApi = ApiClient.getClient(requireContext()).create(BillingApi.class);
            billingApi.getBill(s.patientId).enqueue(new Callback<BillResponse>() {
                @Override
                public void onResponse(Call<BillResponse> call, Response<BillResponse> response) {
                    loadingDialog.dismiss();
                    if (response.isSuccessful() && response.body() != null) {
                        handleBillResponse(response.body(), true);
                    } else {
                        loadOfflineBill();
                    }
                }

                @Override
                public void onFailure(Call<BillResponse> call, Throwable t) {
                    loadingDialog.dismiss();
                    loadOfflineBill();
                }
            });
        } else {
            loadOfflineBill();
        }
    }

    private void handleBillResponse(BillResponse bill, boolean saveToCache) {
        PatientSession s = PatientSession.getInstance();
        s.billId = bill.getId();
        s.totalBill = bill.getTotalAmount();
        s.insuranceCoverage = bill.getInsuranceCover();
        s.netPayable = bill.getNetPayable();

        updateUI();

        if (billContainer != null) {
            billContainer.removeAllViews();
            if (bill.getItems() != null && !bill.getItems().isEmpty()) {
                for (BillResponse.BillItem item : bill.getItems()) {
                    addBillRow(item.getName(), item.getDate(), item.getAmount());
                    if (saveToCache && s.patientId != null) {
                        dbHelper.saveBillFromApi(bill.getId(), s.patientId, item.getName(), item.getAmount(), item.getDate(), "unpaid");
                    }
                }
            } else {
                showEmptyState("No billing items found.");
            }
        }
    }

    private void loadOfflineBill() {
        Toast.makeText(requireContext(), "Viewing cached data", Toast.LENGTH_SHORT).show();
        PatientSession s = PatientSession.getInstance();
        Cursor cursor = dbHelper.getCachedBill(s.patientId);
        
        if (cursor != null && cursor.getCount() > 0) {
            if (billContainer != null) billContainer.removeAllViews();
            
            double total = 0;
            String billId = "";
            while (cursor.moveToNext()) {
                int itemIdx = cursor.getColumnIndex(DatabaseHelper.COL_ITEM);
                int dateIdx = cursor.getColumnIndex(DatabaseHelper.COL_DATE);
                int amtIdx = cursor.getColumnIndex(DatabaseHelper.COL_AMOUNT);
                int billIdIdx = cursor.getColumnIndex(DatabaseHelper.COL_BILL_ID);
                
                String item = itemIdx != -1 ? cursor.getString(itemIdx) : "Item";
                String date = dateIdx != -1 ? cursor.getString(dateIdx) : "";
                double amount = amtIdx != -1 ? cursor.getDouble(amtIdx) : 0.0;
                billId = billIdIdx != -1 ? cursor.getString(billIdIdx) : "N/A";
                
                total += amount;
                addBillRow(item, date, amount);
            }
            cursor.close();
            
            s.billId = billId;
            s.totalBill = total;
            s.insuranceCoverage = total * 0.8; // mock 80% coverage
            s.netPayable = total - s.insuranceCoverage;
            updateUI();
        } else {
            showEmptyState("No offline bill found.");
        }
    }

    private void updateUI() {
        PatientSession s = PatientSession.getInstance();
        if (tvTotal != null) tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", s.totalBill));
        if (tvNetTop != null) tvNetTop.setText(String.format(Locale.getDefault(), "$%.2f", s.netPayable));
        if (tvSubtotal != null) tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", s.totalBill));
        if (tvInsuranceLabel != null) tvInsuranceLabel.setText((s.insuranceProvider != null ? s.insuranceProvider : "Insurance") + " Cover");
        if (tvInsuranceAmount != null) tvInsuranceAmount.setText(String.format(Locale.getDefault(), "-$%.2f", s.insuranceCoverage));
        if (tvBillTotal != null) tvBillTotal.setText(String.format(Locale.getDefault(), "$%.2f", s.netPayable));
    }

    private void showEmptyState(String message) {
        if (billContainer != null) {
            billContainer.removeAllViews();
            TextView tv = new TextView(requireContext());
            tv.setText(message);
            tv.setPadding(16, 16, 16, 16);
            billContainer.addView(tv);
        }
    }

    private void addBillRow(String item, String date, double amount) {
        if (!isAdded() || getContext() == null) return;
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View row = inflater.inflate(R.layout.item_bill_row, billContainer, false);
        TextView tvName = row.findViewById(R.id.tv_bill_item_name);
        TextView tvDate = row.findViewById(R.id.tv_bill_item_date);
        TextView tvAmount = row.findViewById(R.id.tv_bill_item_amount);
        
        if (tvName != null) tvName.setText(item);
        if (tvDate != null) tvDate.setText(date);
        if (tvAmount != null) tvAmount.setText(String.format(Locale.getDefault(), "$%.2f", amount));
        
        billContainer.addView(row);

        View divider = new View(requireContext());
        divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
        divider.setBackgroundColor(getResources().getColor(R.color.divider, null));
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) divider.getLayoutParams();
        lp.setMargins(0, 16, 0, 16);
        divider.setLayoutParams(lp);
        billContainer.addView(divider);
    }

    private void showQrDialog() {
        if (getContext() == null) return;
        PatientSession s = PatientSession.getInstance();
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_qr_code);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        TextView tvBillId = dialog.findViewById(R.id.tv_qr_bill_id);
        TextView tvTotal = dialog.findViewById(R.id.tv_qr_total);
        if (tvBillId != null) tvBillId.setText("Bill ID: #" + s.billId);
        if (tvTotal != null) tvTotal.setText(String.format(Locale.getDefault(), "Payable: $%.2f", s.netPayable));
        dialog.findViewById(R.id.btn_close_qr).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
