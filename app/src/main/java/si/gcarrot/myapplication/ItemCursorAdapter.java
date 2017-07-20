package si.gcarrot.myapplication;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import si.gcarrot.myapplication.data.ItemContract.ItemEntry;

/**
 * Created by Urban on 7/19/17.
 */

public class ItemCursorAdapter extends CursorAdapter {


    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }


    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView text_name = (TextView) view.findViewById(R.id.name);
        TextView text_price = (TextView) view.findViewById(R.id.price);
        final TextView text_quantity = (TextView) view.findViewById(R.id.quantity);

        final Button btnSell = (Button) view.findViewById(R.id.btn_sell);

        final int IDColumnIndex = cursor.getColumnIndex(ItemEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
        final int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);

        String itemName = cursor.getString(nameColumnIndex);
        double itemPrice = cursor.getDouble(priceColumnIndex);
        int itemQuantity = cursor.getInt(quantityColumnIndex);

        text_name.setText(itemName);
        text_price.setText(context.getResources().getString(R.string.price) + " " + String.valueOf(itemPrice) + "€");
        text_quantity.setText(String.valueOf(itemQuantity));

        final int position = cursor.getPosition();
        if(cursor.getInt(quantityColumnIndex) > 0){
            btnSell.setVisibility(View.VISIBLE);
        }

        btnSell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cursor.moveToPosition(position);
                int oldNum = (cursor.getInt(quantityColumnIndex));
                if (oldNum > 0) {
                    btnSell.setVisibility(View.VISIBLE);
                    oldNum -= 1;
                    if (oldNum >= 1) {

                        ContentValues contentValues = new ContentValues();
                        contentValues.put(ItemEntry.COLUMN_ITEM_QUANTITY, oldNum);

                        String whereStatment = ItemEntry._ID + " =?";

                        int item_id = cursor.getInt(IDColumnIndex);

                        String itemIDStatmen = Integer.toString(item_id);
                        String[] selectionStatment = {itemIDStatmen};

                        view.getContext().getContentResolver().update(
                                ContentUris.withAppendedId(ItemEntry.CONTENT_URI, item_id),
                                contentValues,
                                 whereStatment, selectionStatment);


                        text_quantity.setText(String.valueOf(oldNum));
                    } else {
                        // Disable sell btn
                        btnSell.setVisibility(View.GONE);
                    }

                } else {
                    btnSell.setVisibility(View.GONE);
                }

            }
        });
    }
}