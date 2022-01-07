package com.app.tnevi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.tnevi.Adapters.CardAdapter;
import com.app.tnevi.Adapters.FaqAdapter;
import com.app.tnevi.Allurl.Allurl;
import com.app.tnevi.internet.CheckConnectivity;
import com.app.tnevi.model.FaqModel;
import com.app.tnevi.model.GeteventModel;
import com.app.tnevi.model.SaveCardsModel;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.exception.AuthenticationException;
import com.stripe.android.exception.StripeException;
import com.stripe.android.model.Card;

import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class activity_checkout extends AppCompatActivity {


    private static final String TAG = "myapp";
    CardInputWidget mCardInputWidget;
    LinearLayout payButton;
    Card card;
    String stripetoken = "";
    String eventname, date, address, total, postedby, rowid, blockid, eventid, latvalue,
            lonvalue, currencyid, fees, seatnumber, authtoken, dis_amount, tax, email, zip, state;
    private static final String SHARED_PREFS = "sharedPrefs";
    String tran_id, inv_id, source, brand, last4, exp_month, exp_year;
    RecyclerView rv_cards;
    private CardAdapter cardAdapter;
    private ArrayList<SaveCardsModel> saveCardsModelArrayList;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        Intent intent = getIntent();
        eventname = intent.getStringExtra("eventname");
        date = intent.getStringExtra("date");
        address = intent.getStringExtra("address");
        total = intent.getStringExtra("total");
        postedby = intent.getStringExtra("name");
        rowid = intent.getStringExtra("rowid");
        blockid = intent.getStringExtra("blockid");
        eventid = intent.getStringExtra("eventid");
        latvalue = intent.getStringExtra("latvalue");
        lonvalue = intent.getStringExtra("lonvalue");
        currencyid = intent.getStringExtra("currencyid");
        fees = intent.getStringExtra("fees");
        seatnumber = intent.getStringExtra("seatnumber");
        tax = intent.getStringExtra("tax");
        dis_amount= intent.getStringExtra("dis_amount");
        zip = intent.getStringExtra("zip");
        state = intent.getStringExtra("state");
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        authtoken = sharedPreferences.getString("token", "");
        email = sharedPreferences.getString("email", "");

        mCardInputWidget = findViewById(R.id.cardInputWidget);
        payButton = findViewById(R.id.payButton);
        rv_cards = findViewById(R.id.rv_cards);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mCardInputWidget.getCard() != null) {
                    String cvv = mCardInputWidget.getCard().getCVC();
                    int exp = mCardInputWidget.getCard().getExpMonth();
                    int exp_year = mCardInputWidget.getCard().getExpYear();
                    String card_num = mCardInputWidget.getCard().getNumber();
                    card = new Card(card_num, exp, exp_year, cvv);
                    Stripe stripe = new Stripe(getApplicationContext(), "pk_test_51K9MAmDWSUf4dQGJudEZMh1qkrQ9AIsVrvauopYN1ec05IN78YUg8uQ2RQAP231S6uYRDgd83rO2KkeCQvy7OjW600IwIiwpbU");
                    stripe.createToken(card, new TokenCallback() {
                                public void onSuccess(Token token) {
                                    Log.d("Stripe Token", token.getId());
                                    stripetoken = token.getId();
                                    Log.d(TAG, "tokenstripe-->" + stripetoken);
                                    if (stripetoken.length() > 0) {
                                        stripepayment();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Token not generated successfully!", Toast.LENGTH_LONG).show();
                                    }
                                }

                                public void onError(Exception error) {
                                    Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                    );

                }


            }
        });

        savecards();

    }


    public void savecards(){

        if (CheckConnectivity.getInstance(getApplicationContext()).isOnline()) {


            showProgressDialog();

            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, Allurl.SaveCards, null, response -> {

                Log.i("Response-->", String.valueOf(response));

                try {
                    JSONObject result = new JSONObject(String.valueOf(response));
                    String msg = result.getString("message");
                    Log.d(TAG, "msg-->" + msg);
                    String stat = result.getString("stat");
                    if (stat.equals("succ")) {

                        saveCardsModelArrayList = new ArrayList<>();
                        JSONArray response_data = result.getJSONArray("data");
                        for (int i = 0; i < response_data.length(); i++) {

                            SaveCardsModel saveCardsModel = new SaveCardsModel();
                            JSONObject responseobj = response_data.getJSONObject(i);
                            saveCardsModel.setId(responseobj.getString("id"));
                            saveCardsModel.setUser_id(responseobj.getString("user_id"));
                            saveCardsModel.setStripeToken(responseobj.getString("stripeToken"));
                            saveCardsModel.setCard_id(responseobj.getString("card_id"));
                            saveCardsModel.setCust_id(responseobj.getString("cust_id"));
                            saveCardsModel.setBrand(responseobj.getString("brand"));
                            saveCardsModel.setExp_month(responseobj.getString("exp_month"));
                            saveCardsModel.setExp_year(responseobj.getString("exp_year"));
                            saveCardsModel.setLast4(responseobj.getString("last4"));
                            saveCardsModel.setCountry(responseobj.getString("country"));
                            saveCardsModel.setName(responseobj.getString("name"));
                            saveCardsModel.setEmail(responseobj.getString("email"));
                            saveCardsModelArrayList.add(saveCardsModel);

                        }

                        cardAdapter = new CardAdapter(this, saveCardsModelArrayList);
                        rv_cards.setAdapter(cardAdapter);
                        rv_cards.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));


                    } else {

                        Log.d(TAG, "unsuccessfull - " + "Error");
                        Toast.makeText(activity_checkout.this, "invalid", Toast.LENGTH_SHORT).show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                hideProgressDialog();

                //TODO: handle success
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(activity_checkout.this, error.toString(), Toast.LENGTH_SHORT).show();

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", authtoken);
                    return params;
                }
            };

            Volley.newRequestQueue(this).add(jsonRequest);

        } else {

            Toast.makeText(getApplicationContext(), "Ooops! Internet Connection Error", Toast.LENGTH_SHORT).show();

        }
    }

    public void stripepayment(){

        if (CheckConnectivity.getInstance(getApplicationContext()).isOnline()) {
            showProgressDialog();
            JSONObject params = new JSONObject();

            try {
                params.put("stripeToken", stripetoken);
                params.put("currency_id", "1");
                params.put("amount", total);
                params.put("description", "buy ticket");


            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, Allurl.StripePayment, params, response -> {

                Log.i("Response-->", String.valueOf(response));

                try {
                    JSONObject result = new JSONObject(String.valueOf(response));
                    String msg = result.getString("message");
                    String stat = result.getString("stat");
                    if (stat.equals("succ")) {

                        JSONObject data = result.getJSONObject("data");
                        tran_id = data.getString("tran_id");
                        inv_id = data.getString("inv_id");
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        bookticket();

                    } else {

                        hideProgressDialog();
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


                hideProgressDialog();

                //TODO: handle success
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hideProgressDialog();
                    Toast.makeText(activity_checkout.this, error.toString(), Toast.LENGTH_SHORT).show();

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", authtoken);
                    return params;
                }
            };

            Volley.newRequestQueue(this).add(jsonRequest);

        } else {

            Toast.makeText(getApplicationContext(), "Ooops! Internet Connection Error", Toast.LENGTH_SHORT).show();

        }
    }

    public void savecardpayment(SaveCardsModel saveCardsModel){

        if (CheckConnectivity.getInstance(getApplicationContext()).isOnline()) {
            showProgressDialog();
            JSONObject params = new JSONObject();

            try {
                params.put("cust_id", saveCardsModel.getCust_id());
                params.put("currency_id", "1");
                params.put("amount", total);
                params.put("description", "buy ticket");


            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, Allurl.StripePayment, params, response -> {

                Log.i("Response-->", String.valueOf(response));

                try {
                    JSONObject result = new JSONObject(String.valueOf(response));
                    String msg = result.getString("message");
                    String stat = result.getString("stat");
                    if (stat.equals("succ")) {

                        JSONObject data = result.getJSONObject("data");
                        tran_id = data.getString("tran_id");
                        inv_id = data.getString("inv_id");
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        bookticket();

                    } else {

                        hideProgressDialog();
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


                hideProgressDialog();

                //TODO: handle success
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hideProgressDialog();
                    Toast.makeText(activity_checkout.this, error.toString(), Toast.LENGTH_SHORT).show();

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", authtoken);
                    return params;
                }
            };

            Volley.newRequestQueue(this).add(jsonRequest);

        } else {

            Toast.makeText(getApplicationContext(), "Ooops! Internet Connection Error", Toast.LENGTH_SHORT).show();

        }

    }

    public void bookticket() {


        if (CheckConnectivity.getInstance(getApplicationContext()).isOnline()) {
            showProgressDialog();
            JSONObject params = new JSONObject();

            try {
                params.put("event_id", eventid);
                params.put("block_id", blockid);
                params.put("row_id", rowid);
                params.put("no_of_seat", seatnumber);
                params.put("name_on_ticket", postedby);
                params.put("full_name", postedby);
                params.put("book_address", address);
                params.put("book_state", state);
                params.put("book_zipcode", zip);
                params.put("book_lat_val", latvalue);
                params.put("book_long_val", lonvalue);
                params.put("currency_id", currencyid);
                params.put("fees", fees);
                params.put("grand_total", total);
                params.put("tran_id", tran_id);
                params.put("inv_id", inv_id);
                params.put("payer_email", email);
                params.put("payment_amount ", total);
                params.put("apply_epoints ", "");
                params.put("epoints_discount ", "");
                params.put("commission_discount ", "");
                params.put("ticket_discount", "");
                params.put("coupon_code", "");
                params.put("coupon_discount", "");
                params.put("total_discount", dis_amount);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, Allurl.BookTicket, params, response -> {

                Log.i("Response-->", String.valueOf(response));

                try {
                    JSONObject result = new JSONObject(String.valueOf(response));
                    String msg = result.getString("message");
                    String stat = result.getString("stat");
                    if (stat.equals("succ")) {

                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(activity_checkout.this, MainActivity.class);
                        startActivity(intent);

                    } else {

                        hideProgressDialog();
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


                hideProgressDialog();

                //TODO: handle success
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hideProgressDialog();
                    Toast.makeText(activity_checkout.this, error.toString(), Toast.LENGTH_SHORT).show();

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", authtoken);
                    return params;
                }
            };

            Volley.newRequestQueue(this).add(jsonRequest);

        } else {

            Toast.makeText(getApplicationContext(), "Ooops! Internet Connection Error", Toast.LENGTH_SHORT).show();

        }


    }


    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }


}