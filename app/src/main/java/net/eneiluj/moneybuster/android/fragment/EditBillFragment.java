package net.eneiluj.moneybuster.android.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.eneiluj.moneybuster.R;
import net.eneiluj.moneybuster.android.ui.TextDrawable;
import net.eneiluj.moneybuster.android.ui.UserAdapter;
import net.eneiluj.moneybuster.android.ui.UserItem;
import net.eneiluj.moneybuster.model.DBBill;
import net.eneiluj.moneybuster.model.DBBillOwer;
import net.eneiluj.moneybuster.model.DBMember;
import net.eneiluj.moneybuster.model.ProjectType;
import net.eneiluj.moneybuster.persistence.MoneyBusterSQLiteOpenHelper;
import net.eneiluj.moneybuster.util.ICallback;
import net.eneiluj.moneybuster.util.MoneyBuster;
import net.eneiluj.moneybuster.util.ThemeUtils;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditBillFragment extends Fragment {

    private static final String TAG = EditBillFragment.class.getSimpleName();
    private ProjectType projectType;

    public interface BillFragmentListener {
        void close();

        void closeOnDelete(long billId);

        void onBillUpdated(DBBill bill);
    }

    private static final String PARAM_BILL_ID = "billId";
    private static final String PARAM_NEWBILL = "newBill";
    private static final String PARAM_PROJECT_TYPE = "projectType";
    private static final String SAVEDKEY_BILL = "bill";
    private static final String SAVEDKEY_PROJECT_TYPE = "type";
    private static final String SAVEDKEY_ORIGINAL_BILL = "original_bill";

    protected DBBill bill;

    private MoneyBusterSQLiteOpenHelper db;
    private BillFragmentListener listener;

    private Handler handler;

    private EditText editWhat;
    private EditText editDate;
    private String isoDate;
    private Spinner editPayer;
    private EditText editAmount;
    private Spinner editRepeat;
    private Spinner editPaymentMode;
    private Spinner editCategory;
    private LinearLayout owersLayout;
    private FloatingActionButton fabSaveBill;
    private Button bAll;
    private Button bNone;
    private LinearLayout editRepeatLayout;
    private LinearLayout editPaymentModeLayout;
    private LinearLayout editCategoryLayout;

    private Calendar calendar;
    private DatePickerDialog datePickerDialog;

    private Map<Long, CheckBox> owerCheckboxes;

    private DialogInterface.OnClickListener deleteDialogClickListener;
    private AlertDialog.Builder confirmDeleteAlertBuilder;

    private SimpleDateFormat sdf;

    private boolean isSpinnerUserAction = false;
    private boolean isSpinnerRepeatAction = false;
    private boolean isSpinnerPaymentModeAction = false;
    private boolean isSpinnerCategoryAction = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_edit_bill_form, container, false);
        editWhat = view.findViewById(R.id.editWhat);
        editAmount = view.findViewById(R.id.editAmount);
        editDate = view.findViewById(R.id.editDate);
        editDate.setFocusable(false);
        editPayer = view.findViewById(R.id.editPayerSpinner);
        owersLayout = view.findViewById(R.id.owerListLayout);
        editRepeat = view.findViewById(R.id.editRepeatSpinner);
        editPaymentMode = view.findViewById(R.id.editPaymentModeSpinner);
        editCategory = view.findViewById(R.id.editCategorySpinner);
        editRepeatLayout = view.findViewById(R.id.editRepeatLayout);
        editPaymentModeLayout = view.findViewById(R.id.editPaymentModeLayout);
        editCategoryLayout = view.findViewById(R.id.editCategoryLayout);
        fabSaveBill = view.findViewById(R.id.fab_edit_ok);

        // color
        boolean darkTheme = MoneyBuster.getAppTheme(getContext());
        // if dark theme and main color is black, make fab button lighter/gray
        if (darkTheme && ThemeUtils.primaryColor(getContext()) == Color.BLACK) {
            fabSaveBill.setBackgroundTintList(ColorStateList.valueOf(Color.DKGRAY));
        } else {
            fabSaveBill.setBackgroundTintList(ColorStateList.valueOf(ThemeUtils.primaryColor(getContext())));
        }
        fabSaveBill.setRippleColor(ThemeUtils.primaryDarkColor(getContext()));

        fabSaveBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBillAsked();
            }
        });

        bAll = view.findViewById(R.id.owerAllButton);
        bAll.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                for (Map.Entry<Long, CheckBox> entry : owerCheckboxes.entrySet()) {
                    entry.getValue().setChecked(true);
                }
            }
        });
        bNone = view.findViewById(R.id.owerNoneButton);
        bNone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                for (Map.Entry<Long, CheckBox> entry : owerCheckboxes.entrySet()) {
                    entry.getValue().setChecked(false);
                }
            }
        });
        if (darkTheme) {
            bAll.setTextColor(ColorStateList.valueOf(Color.BLACK));
            bNone.setTextColor(ColorStateList.valueOf(Color.BLACK));
        }

        calendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        datePickerDialog = new DatePickerDialog(EditBillFragment.this.getContext(), date, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)) {

            @Override
            public void onDateChanged(DatePicker view,
                                      int year,
                                      int month,
                                      int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
                // to make sure we don't get out of old-style date picker dialogs
                //datePickerDialog.dismiss();
            }
        };


        editDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        // on value changes

        editWhat.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "WHWHWHWHAAAATTT");
                showHideValidationButtons();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        editAmount.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "AMOUNTTTT");
                showHideValidationButtons();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        editPayer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Log.d(TAG, "PAYERRRR");
                if (isSpinnerUserAction) {
                    showHideValidationButtons();
                }

                isSpinnerUserAction = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                Log.d(TAG, "PAYERRRR NOTHING");
                showHideValidationButtons();
            }
        });

        editRepeat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Log.d(TAG, "REPEAT");
                if (isSpinnerRepeatAction) {
                    showHideValidationButtons();
                }

                isSpinnerRepeatAction = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                Log.d(TAG, "REPEAT NOTHING");
                showHideValidationButtons();
            }

        });

        editPaymentMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Log.d(TAG, "PAYMENT MODE");
                if (isSpinnerPaymentModeAction) {
                    showHideValidationButtons();
                }

                isSpinnerPaymentModeAction = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                Log.d(TAG, "PAYMENT MODE NOTHING");
                showHideValidationButtons();
            }

        });

        editCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Log.d(TAG, "CATEGORY");
                if (isSpinnerCategoryAction) {
                    showHideValidationButtons();
                }

                isSpinnerCategoryAction = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                Log.d(TAG, "CATEGORY NOTHING");
                showHideValidationButtons();
            }

        });

        Log.d(TAG, "CREATEVIEW FINISHEDDDDDDD");

        return view;
    }

    private void updateLabel() {
        setDateFromIso(sdf.format(calendar.getTime()));
        Log.d(TAG, "DATEUUUU");
        showHideValidationButtons();
    }

    private void showHideAllNoneButtons() {
        Log.d(TAG, "SHOWHIDEALLNONE ");
        int nbChecked = 0;
        int nbTot = 0;

        for (Map.Entry<Long, CheckBox> entry : owerCheckboxes.entrySet()) {
            nbTot++;
            if (entry.getValue().isChecked()) {
                nbChecked++;
            }
        }
        bAll.setVisibility((nbChecked < nbTot) ? View.VISIBLE : View.GONE);
        bNone.setVisibility((nbChecked > 0) ? View.VISIBLE : View.GONE);
    }

    private void showHideValidationButtons() {
        if (hideSaveButton()) {
            fabSaveBill.hide();
        } else {
            fabSaveBill.show();
        }
    }

    private boolean hideSaveButton() {
        return getWhat() == null || getWhat().equals("") ||
                getDate() == null || getDate().equals("") ||
                getAmount() == 0.0 ||
                getPayerId() == 0 ||
                getOwersIds().size() == 0;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);

        if (savedInstanceState == null) {
            long id = getArguments().getLong(PARAM_BILL_ID);
            projectType = ProjectType.getTypeById(getArguments().getString(PARAM_PROJECT_TYPE, ProjectType.LOCAL.getId()));
            if (id > 0) {
                bill = db.getBill(id);
                listener.onBillUpdated(bill);
            } else {
                DBBill newBill = (DBBill) getArguments().getSerializable(PARAM_NEWBILL);
                if (newBill == null) {
                    throw new IllegalArgumentException(PARAM_BILL_ID + " is not given and argument " + PARAM_NEWBILL + " is missing.");
                }
                //bill = db.getBill(db.addBill(cloudBill));
                bill = newBill;
            }
        } else {
            bill = (DBBill) savedInstanceState.getSerializable(SAVEDKEY_BILL);
            projectType = (ProjectType) savedInstanceState.getSerializable(SAVEDKEY_PROJECT_TYPE);
        }
        setHasOptionsMenu(true);
        Log.d(TAG, "BILL on create : " + bill);

        // delete confirmation
        deleteDialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        if (db.getBill(bill.getId()).getState() == DBBill.STATE_ADDED) {
                            db.deleteBill(bill.getId());
                        } else {
                            db.setBillState(bill.getId(), DBBill.STATE_DELETED);
                        }
                        listener.closeOnDelete(bill.getId());
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        confirmDeleteAlertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this.getActivity(), R.style.AppThemeDialog));
        confirmDeleteAlertBuilder.setMessage(getString(R.string.confirm_delete_bill_dialog_title))
                .setPositiveButton(getString(R.string.simple_yes), deleteDialogClickListener)
                .setNegativeButton(getString(R.string.simple_no), deleteDialogClickListener);

        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (BillFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass() + " must implement " + BillFragmentListener.class);
        }
        db = MoneyBusterSQLiteOpenHelper.getInstance(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        //listener.onBillUpdated(bill);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //saveBill(null);
        outState.putSerializable(SAVEDKEY_BILL, bill);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_bill_fragment, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (bill.getId() < 1) {
            menu.removeItem(R.id.menu_delete);
        }
    }

    private void saveBillAsked() {
        if (getWhat() == null || getWhat().equals("")) {
            showToast(getString(R.string.error_invalid_bill_what), Toast.LENGTH_LONG);
        } else if (getDate() == null || getDate().equals("")) {
            showToast(getString(R.string.error_invalid_bill_date), Toast.LENGTH_LONG);
        } else if (getAmount() == 0.0) {
            showToast(getString(R.string.error_invalid_bill_amount), Toast.LENGTH_LONG);
        } else if (getPayerId() == 0) {
            showToast(getString(R.string.error_invalid_bill_payerid), Toast.LENGTH_LONG);
        } else if (getOwersIds().size() == 0) {
            showToast(getString(R.string.error_invalid_bill_owers), Toast.LENGTH_LONG);
        } else {
            saveBill(null);
            listener.close();
        }
    }

    /**
     * Main-Menu-Handler
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                if (bill.getId() != 0) {
                    confirmDeleteAlertBuilder.show();
                } else {
                    listener.close();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onCloseBill() {
        Log.d(getClass().getSimpleName(), "onCLOSE()");
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    /**
     * Save the current state in the database and schedule synchronization if needed.
     *
     * @param callback Observer which is called after save/synchronization
     */

    protected void saveBill(@Nullable ICallback callback) {
        Log.d(getClass().getSimpleName(), "CUSTOM saveData()");
        String newWhat = getWhat();
        String newDate = getDate();
        double newAmount = getAmount();
        long newPayerId = getPayerId();
        String newRepeat = DBBill.NON_REPEATED;
        String newPaymentMode = DBBill.PAYMODE_NONE;
        int newCategoryId = DBBill.CATEGORY_NONE;

        if (ProjectType.COSPEND.equals(projectType)) {
            newRepeat = getRepeat();
            newPaymentMode = getPaymentMode();
            newCategoryId = getCategoryId();
        }

        List<Long> newOwersIds = getOwersIds();

        // check if owers have changed
        boolean owersChanged = false;
        List<Long> billOwersIds = bill.getBillOwersIds();
        if (newOwersIds.size() != billOwersIds.size()) {
            owersChanged = true;
        } else {
            if (!newOwersIds.containsAll(billOwersIds)) {
                owersChanged = true;
            }
            if (!billOwersIds.containsAll(newOwersIds)) {
                owersChanged = true;
            }
        }

        // if this is an existing bill
        if (bill.getId() != 0) {
            if (bill.getWhat().equals(newWhat) &&
                    bill.getDate().equals(newDate) &&
                    bill.getAmount() == newAmount &&
                    bill.getPayerId() == newPayerId &&
                    newRepeat.equals(bill.getRepeat()) &&
                    newPaymentMode.equals(bill.getPaymentMode()) &&
                    newCategoryId == bill.getCategoryId() &&
                    !owersChanged
            ) {
                Log.v(getClass().getSimpleName(), "... not saving bill, since nothing has changed " + bill.getWhat() + " " + newWhat);
            } else {
                Log.d(TAG, "====== update bill");
                db.updateBillAndSync(bill, newPayerId, newAmount, newDate, newWhat, newOwersIds,
                                     newRepeat, newPaymentMode, newCategoryId);
                //listener.onBillUpdated(bill);
                //listener.close();
            }
        }
        // this is a new bill
        else {
            // add the bill
            DBBill newBill = new DBBill(0, 0, bill.getProjectId(), newPayerId, newAmount,
                    newDate, newWhat, DBBill.STATE_ADDED, newRepeat, newPaymentMode, newCategoryId);
            for (long newOwerId : newOwersIds) {
                newBill.getBillOwers().add(new DBBillOwer(0, 0, newOwerId));
            }
            long newBillId = db.addBill(newBill);

            // update last payer id
            db.updateProject(
                    bill.getProjectId(), null, null,
                    null, newPayerId,null
            );

            // normally sync should be done when we get back to bill list
            // so next line can be commented
            // db.getMoneyBusterServerSyncHelper().scheduleSync(true, bill.getProjectId());
        }
    }

    public static EditBillFragment newInstance(long billId, ProjectType projectType) {
        EditBillFragment f = new EditBillFragment();
        Bundle b = new Bundle();
        b.putLong(PARAM_BILL_ID, billId);
        b.putString(PARAM_PROJECT_TYPE, projectType.getId());
        f.setArguments(b);
        return f;
    }

    public static EditBillFragment newInstanceWithNewBill(DBBill newBill, ProjectType projectType) {
        EditBillFragment f = new EditBillFragment();
        Bundle b = new Bundle();
        b.putSerializable(PARAM_NEWBILL, newBill);
        b.putString(PARAM_PROJECT_TYPE, projectType.getId());
        f.setArguments(b);
        return f;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "ACT EDIT BILL CREATED");

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // manage member list
        List<DBMember> memberList = db.getMembersOfProject(bill.getProjectId(), null);
        List<String> memberNameList = new ArrayList<>();
        List<String> memberIdList = new ArrayList<>();
        for (DBMember member : memberList) {
            if (member.isActivated()) {
                memberNameList.add(member.getName());
                memberIdList.add(String.valueOf(member.getId()));
            }
        }

        // build ower list
        owerCheckboxes = new HashMap<>();
        for (DBMember member : memberList) {
            if (member.isActivated()) {
                View row = LayoutInflater.from(getContext()).inflate(R.layout.ower_row, null);

                final CheckBox cb = row.findViewById(R.id.owerBox);
                int owerIndex = bill.getBillOwersIds().indexOf(member.getId());
                if (bill.getId() == 0 || owerIndex != -1) {
                    cb.setChecked(true);
                }
                cb.setText(member.getName());
                cb.setTextColor(ContextCompat.getColor(owersLayout.getContext(), R.color.fg_default));
                owerCheckboxes.put(member.getId(), cb);

                // avatar
                ImageView avatar = row.findViewById(R.id.avatar);
                try {
                    avatar.setImageDrawable(
                            TextDrawable.createNamedAvatar(
                                    member.getName(), 30,
                                    member.getR(), member.getG(), member.getB()
                            )
                    );
                    avatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
                } catch (NoSuchAlgorithmException e) {
                    Log.e(TAG, "error creating avatar", e);
                    avatar.setImageDrawable(null);
                }
                // click on avatar
                avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cb.performClick();
                    }
                });


                owersLayout.addView(row);

                // checkbox change listener
                cb.setOnCheckedChangeListener(
                        new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                showHideAllNoneButtons();
                                Log.d(TAG, "OWERRRR");
                                showHideValidationButtons();
                            }
                        }
                );
            }
        }
        showHideAllNoneButtons();

        // build payer list
        if (memberNameList.size() > 0) {
            List<UserItem> userList = new ArrayList<>();
            for (int i = 0; i < memberNameList.size(); i++) {
                userList.add(new UserItem(Long.valueOf(memberIdList.get(i)), memberNameList.get(i)));
            }

            UserAdapter userAdapter = new UserAdapter(this.getActivity(), userList);
            editPayer.setAdapter(userAdapter);
            editPayer.getSelectedItemPosition();

            // set selected value for payer
            if (bill.getPayerId() != 0) {
                String payerId = String.valueOf(bill.getPayerId());
                // if the id is not found, it means the user is disabled
                int payerIndex = memberIdList.indexOf(payerId);
                if (payerIndex != -1) {
                    editPayer.setSelection(payerIndex);
                }
            } else {
                // this is a new bill, we try to put last payer id
                long lastPayerId = db.getProject(bill.getProjectId()).getLastPayerId();
                int payerIndex = memberIdList.indexOf(String.valueOf(lastPayerId));
                if (payerIndex != -1) {
                    editPayer.setSelection(payerIndex);
                }
            }
        }

        // select what and show keyboard if this is a new bill
        if (bill.getId() == 0) {
            editWhat.setSelectAllOnFocus(true);
            editWhat.requestFocus();
            // show keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } else {
            editWhat.setText(bill.getWhat());
        }


        try {
            Date d = sdf.parse(bill.getDate());
            calendar.setTime(d);
            updateLabel();
            datePickerDialog.getDatePicker().updateDate(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
        } catch (ParseException e) {
            Log.d(getClass().getSimpleName(), "bad date " + bill.getDate());
        }

        editAmount.setText(String.valueOf(bill.getAmount()));

        // hide the validation button so that it appears if a value changes

        fabSaveBill.hide();
        Log.d(TAG, "HIIIIIIIIIIDE FAB");

        if (ProjectType.COSPEND.equals(projectType)) {
            List<String> repeatNameList = new ArrayList<>();
            repeatNameList.add(getString(R.string.repeat_no));
            repeatNameList.add(getString(R.string.repeat_day));
            repeatNameList.add(getString(R.string.repeat_week));
            repeatNameList.add(getString(R.string.repeat_month));
            repeatNameList.add(getString(R.string.repeat_year));

            String[] repeatNames = repeatNameList.toArray(new String[repeatNameList.size()]);
            //String[] repeatNames = getResources().getStringArray(R.array.repeatBillEntries);

            String[] repeatIds = getResources().getStringArray(R.array.repeatBillValues);
            int index = Arrays.asList(repeatIds).indexOf(bill.getRepeat());

            ArrayList<Map<String, String>> data = new ArrayList<>();
            for (int i = 0; i < repeatNames.length; i++) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("name", repeatNames[i]);
                hashMap.put("id", repeatIds[i]);
                data.add(hashMap);
            }
            String[] from = {"name", "id"};
            int[] to = new int[]{android.R.id.text1};
            SimpleAdapter simpleAdapter = new SimpleAdapter(this.getContext(), data, android.R.layout.simple_spinner_item, from, to);
            simpleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            editRepeat.setAdapter(simpleAdapter);

            if (index > -1) {
                editRepeat.setSelection(index);
            } else {
                editRepeat.setSelection(0);
            }

            // PAYMENT MODE
            List<String> paymentModeNameList = new ArrayList<>();
            paymentModeNameList.add("❌ "+getString(R.string.payment_mode_none));
            paymentModeNameList.add("\uD83D\uDCB3 "+getString(R.string.payment_mode_credit_card));
            paymentModeNameList.add("\uD83D\uDCB5 "+getString(R.string.payment_mode_cash));
            paymentModeNameList.add("\uD83C\uDFAB "+getString(R.string.payment_mode_check));

            String[] paymentModeNames = paymentModeNameList.toArray(new String[paymentModeNameList.size()]);
            //String[] repeatNames = getResources().getStringArray(R.array.repeatBillEntries);

            String[] paymentModeIds = getResources().getStringArray(R.array.paymentModeValues);
            int indexP = Arrays.asList(paymentModeIds).indexOf(bill.getPaymentMode());

            ArrayList<Map<String, String>> dataP = new ArrayList<>();
            for (int i = 0; i < paymentModeNames.length; i++) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("name", paymentModeNames[i]);
                hashMap.put("id", paymentModeIds[i]);
                dataP.add(hashMap);
            }
            String[] fromP = {"name", "id"};
            int[] toP = new int[]{android.R.id.text1};
            SimpleAdapter simpleAdapterP = new SimpleAdapter(this.getContext(), dataP, android.R.layout.simple_spinner_item, fromP, toP);
            simpleAdapterP.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            editPaymentMode.setAdapter(simpleAdapterP);

            if (indexP > -1) {
                editPaymentMode.setSelection(indexP);
            } else {
                editPaymentMode.setSelection(0);
            }

            // CATEGORY
            List<String> categoryNameList = new ArrayList<>();
            categoryNameList.add("❌ "+getString(R.string.category_none));
            categoryNameList.add("\uD83D\uDED2 "+getString(R.string.category_groceries));
            categoryNameList.add("\uD83C\uDF89 "+getString(R.string.category_leisure));
            categoryNameList.add("\uD83C\uDFE0 "+getString(R.string.category_rent));
            categoryNameList.add("\uD83C\uDF29 "+getString(R.string.category_bills));
            categoryNameList.add("\uD83D\uDEB8 "+getString(R.string.category_excursion));
            categoryNameList.add("\uD83D\uDC9A "+getString(R.string.category_health));
            categoryNameList.add("\uD83D\uDECD "+getString(R.string.category_shopping));
            categoryNameList.add("\uD83D\uDCB0 "+getString(R.string.category_reimbursement));
            categoryNameList.add("\uD83C\uDF74 "+getString(R.string.category_restaurant));
            categoryNameList.add("\uD83D\uDECC "+getString(R.string.category_accomodation));
            categoryNameList.add("\uD83D\uDE8C "+getString(R.string.category_transport));
            categoryNameList.add("\uD83C\uDFBE "+getString(R.string.category_sport));

            String[] categoryNames = categoryNameList.toArray(new String[categoryNameList.size()]);

            String[] categoryIds = getResources().getStringArray(R.array.categoryValues);
            int indexC = Arrays.asList(categoryIds).indexOf(String.valueOf(bill.getCategoryId()));
            Log.d(TAG, "CATTTT of loaded bill "+bill.getCategoryId());

            ArrayList<Map<String, String>> dataC = new ArrayList<>();
            for (int i = 0; i < categoryNames.length; i++) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("name", categoryNames[i]);
                hashMap.put("id", categoryIds[i]);
                dataC.add(hashMap);
            }
            String[] fromC = {"name", "id"};
            int[] toC = new int[]{android.R.id.text1};
            SimpleAdapter simpleAdapterC = new SimpleAdapter(this.getContext(), dataC, android.R.layout.simple_spinner_item, fromC, toC);
            simpleAdapterC.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            editCategory.setAdapter(simpleAdapterC);

            if (indexC > -1) {
                editCategory.setSelection(indexC);
            } else {
                editCategory.setSelection(0);
            }

        } else {
            editRepeatLayout.setVisibility(View.GONE);
            editPaymentModeLayout.setVisibility(View.GONE);
            editCategoryLayout.setVisibility(View.GONE);
        }
    }

    protected String getWhat() {
        return editWhat.getText().toString();
    }

    protected String getDate() {
        return isoDate;
    }

    protected void setDateFromIso(String isoDate) {
        this.isoDate = isoDate;
        try {
            Date date = sdf.parse(isoDate);
            java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(db.getContext());
            editDate.setText(dateFormat.format(date));
        } catch (Exception e) {
            editDate.setText(isoDate);
        }
    }

    protected double getAmount() {
        String amount = editAmount.getText().toString();
        if (amount == null || amount.equals("")) {
            return 0.0;
        }
        try {
            return Double.valueOf(amount.replace(',', '.'));
        } catch (Exception e) {
            return 0.0;
        }
    }

    protected long getPayerId() {
        int i = editPayer.getSelectedItemPosition();
        if (i < 0) {
            return 0;
        } else {
            UserItem item = (UserItem) editPayer.getSelectedItem();
            return item.getId();
        }
    }

    protected List<Long> getOwersIds() {
        List<Long> owersIds = new ArrayList<>();
        for (Map.Entry<Long, CheckBox> entry : owerCheckboxes.entrySet()) {
            if (entry.getValue().isChecked()) {
                owersIds.add(entry.getKey());
            }
            Log.i(TAG, "Key : " + entry.getKey() + " Value : " + entry.getValue().isChecked());
        }
        return owersIds;
    }

    private String getRepeat() {
        int i = editRepeat.getSelectedItemPosition();
        if (i < 0) {
            return DBBill.NON_REPEATED;
        } else {
            Map<String, String> item = (Map<String, String>) editRepeat.getSelectedItem();
            return item.get("id");
        }
    }

    private String getPaymentMode() {
        int i = editPaymentMode.getSelectedItemPosition();
        if (i < 0) {
            return DBBill.PAYMODE_NONE;
        } else {
            Map<String, String> item = (Map<String, String>) editPaymentMode.getSelectedItem();
            return item.get("id");
        }
    }

    private int getCategoryId() {
        int i = editCategory.getSelectedItemPosition();
        if (i < 0) {
            return DBBill.CATEGORY_NONE;
        } else {
            Map<String, String> item = (Map<String, String>) editCategory.getSelectedItem();
            return Integer.valueOf(item.get("id"));
        }
    }

    protected void showToast(CharSequence text, int duration) {
        Context context = getActivity();
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
