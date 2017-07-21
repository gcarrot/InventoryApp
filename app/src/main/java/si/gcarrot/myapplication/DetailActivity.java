package si.gcarrot.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

import si.gcarrot.myapplication.data.ItemContract.ItemEntry;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private String LOG_TAG = DetailActivity.class.getSimpleName();
    private static final int EXISTING_ITEM_LOADER = 0;
    private static final int PHOT_REQUEST = 0;

    private Uri mCurrentItemUri;
    private Uri mCurrentUri;

    /** List of inputs **/
    private EditText mItemName;
    private EditText mItemPrice;
    private EditText mItemQuantity;
    private EditText mItemSupplier;
    private EditText mItemSupplier_phone;
    private EditText mItemSupplier_email;

    private ImageView mItemImg;
    private Button mImageBtn;

    private Button mBtnMinus;
    private Button mBtnPlus;
    private Button mBtnCallSupplier;
    private Button mBtnEmailSupplier;

    private boolean mItemChanged = false;

    // Check if user has add something in input and changed data
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();
        if (mCurrentItemUri == null) {
            // Do things when you don't have data
            setTitle(getString(R.string.title_new_product));
        } else {
            // Do things when you have data
            setTitle(getString(R.string.title_edit_product));

            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }


        /** Connect all the inputs with UI **/
        mItemName = (EditText) findViewById(R.id.editText_item_name);
        mItemPrice = (EditText) findViewById(R.id.editText_item_price);
        mItemQuantity = (EditText) findViewById(R.id.editText_item_quantity);
        mItemSupplier = (EditText) findViewById(R.id.editText_seller_name);
        mItemSupplier_phone = (EditText) findViewById(R.id.editText_seller_phone);
        mItemSupplier_email = (EditText) findViewById(R.id.editText_seller_email);
        mItemImg = (ImageView) findViewById(R.id.image_item);
        mImageBtn = (Button) findViewById(R.id.btnAddImg);

        mBtnMinus = (Button) findViewById(R.id.btn_minus);
        mBtnPlus = (Button) findViewById(R.id.btn_plus);
        mBtnCallSupplier = (Button) findViewById(R.id.btn_call_suplier);
        mBtnEmailSupplier = (Button) findViewById(R.id.btn_send_email);


        /** Add all input's on touch listener **/
        mItemName.setOnTouchListener(mTouchListener);
        mItemPrice.setOnTouchListener(mTouchListener);
        mItemQuantity.setOnTouchListener(mTouchListener);
        mItemSupplier.setOnTouchListener(mTouchListener);
        mItemSupplier_phone.setOnTouchListener(mTouchListener);
        mItemSupplier_email.setOnTouchListener(mTouchListener);


        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPhotos();
                mItemChanged = true;
            }
        });

        mBtnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                minusQuntity();
                mItemChanged = true;
            }
        });

        mBtnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                plusQuntity();
                mItemChanged = true;
            }
        });



        mBtnCallSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callSupplier();
            }
        });

        mBtnEmailSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailSupplier();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveItem();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mItemChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mItemChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    // User clicked discard button
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE,
                ItemEntry.COLUMN_ITEM_PICTURE,
                ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL,
                ItemEntry.COLUMN_ITEM_SUPPLIER};


        return new CursorLoader(this, mCurrentItemUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int supplier_phoneColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE);
            int pictureColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PICTURE);
            int supplier_emailColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL);
            int supplierColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER);

            String name = cursor.getString(nameColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantityItem = cursor.getInt(quantityColumnIndex);
            byte[] picture = cursor.getBlob(pictureColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String supplier_phone = cursor.getString(supplier_phoneColumnIndex);
            String supplier_email = cursor.getString(supplier_emailColumnIndex);

            mItemName.setText(name);
            mItemPrice.setText(String.valueOf(price));
            mItemQuantity.setText(String.valueOf(quantityItem));
            if (picture != null) {

                Bitmap img = BitmapFactory.decodeByteArray(picture, 0, picture.length);
                Drawable dIMG = new BitmapDrawable(getResources(), img);
                Log.i("Urban", "testimage: " + img);
                mItemImg.setImageDrawable(dIMG);
            }

            mItemSupplier.setText(supplier);
            mItemSupplier_email.setText(supplier_email);
            mItemSupplier_phone.setText(supplier_phone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mItemName.setText("");
        mItemPrice.setText("");
        mItemQuantity.setText("0");
        mItemSupplier.setText("");
        mItemSupplier_email.setText("");
        mItemSupplier_phone.setText("");
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If user cancel selection then dont do noting
        if(data != null) {
            mCurrentUri = data.getData();
            getImage(mCurrentUri);
        }

    }

    public void getImage(Uri uri) {
        try
        {
            Bitmap bounds = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

            setImage(bounds);
        }
        catch (Exception e)
        {
            //handle exception
            Log.e(LOG_TAG, "Error while uploading image");
        }

    }

    public void setImage(Bitmap image) {
        mItemImg.setImageBitmap(image);
    }

    private void saveItem() {
        String nameString = mItemName.getText().toString().trim();
        String priceString = mItemPrice.getText().toString().trim();
        String quantityString = mItemQuantity.getText().toString().trim();

        String sellerString = mItemSupplier.getText().toString().trim();
        String sellerPhoneString = mItemSupplier_phone.getText().toString().trim();
        String sellerEmailString = mItemSupplier_email.getText().toString().trim();

        BitmapDrawable drawable = (BitmapDrawable) mItemImg.getDrawable();

        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageInByte = stream.toByteArray();


        if (mCurrentItemUri == null ||
                TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString)) {
            // Noting to do here because there are no changes
            Toast.makeText(this, getString(R.string.input_validation_empty),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, nameString);

        if (imageInByte != null) {
            values.put(ItemEntry.COLUMN_ITEM_PICTURE, imageInByte);
        }

        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER, sellerString);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL, sellerEmailString);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE, sellerPhoneString);

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);

        double price = 0.0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }

        values.put(ItemEntry.COLUMN_ITEM_PRICE, price);



        if (mCurrentItemUri == null) {
            // INSERT new item
            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // UPDATE existing item with update
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void getPhotos() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.dialog_select_image));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, PHOT_REQUEST);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog);
        builder.setPositiveButton(R.string.action_discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.action_keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog);
        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void minusQuntity() {
        mItemChanged = true;
        String quantityString = mItemQuantity.getText().toString().trim();
        int quantity = Integer.parseInt(quantityString);
        quantity -= 1;

        if (quantity < 0) {
            quantity = 0;
        }

        mItemQuantity.setText(String.valueOf(quantity));
    }

    private void plusQuntity() {
        mItemChanged = true;
        String quantityString = mItemQuantity.getText().toString().trim();
        int quantity = Integer.parseInt(quantityString);
        quantity += 1;

        if (quantity < 0) {
            quantity = 0;
        }

        mItemQuantity.setText(String.valueOf(quantity));

    }

    private void deleteItem() {
        // Delete only for existing item,
        if (mCurrentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    private void callSupplier() {
        String phone_number = mItemSupplier_phone.getText().toString().trim();
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone_number, null));
        startActivity(intent);
    }

    private void emailSupplier(){
        saveItem();

        String itemName = mItemName.getText().toString().trim();
        String email = mItemSupplier_email.getText().toString().trim();
        String subject = getString(R.string.subject_item_sold_out) + itemName;

        String msg = getString(R.string.msg_item_sold_out) + "";

        Intent intent = new Intent(android.content.Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.setData(Uri.parse("mailto:" + email));
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(android.content.Intent.EXTRA_TEXT, msg);
        startActivity(intent);
    }

}
