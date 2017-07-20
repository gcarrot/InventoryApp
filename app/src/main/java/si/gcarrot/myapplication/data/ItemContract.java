package si.gcarrot.myapplication.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Urban on 7/18/17.
 */

public class ItemContract {

    private ItemContract() {}

    public static final String CONTENT_AUTHORITY = "si.gcarrot.myapplication";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PRODUCTS = "products";

    public static final class ItemEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public final static String TABLE_NAME = "products";

        public final static String COLUMN_ITEM_NAME ="name";

        public final static String COLUMN_ITEM_PRICE = "price";

        public final static String COLUMN_ITEM_QUANTITY = "quantity";

        public final static String COLUMN_ITEM_PICTURE = "picture";

        public final static String COLUMN_ITEM_SUPPLIER = "supplier";

        public final static String COLUMN_ITEM_SUPPLIER_PHONE = "supplier_phone";

        public final static String COLUMN_ITEM_SUPPLIER_EMAIL = "supplier_email";

    }
}
