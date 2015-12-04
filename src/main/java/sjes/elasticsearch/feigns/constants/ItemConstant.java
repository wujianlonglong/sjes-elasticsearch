package sjes.elasticsearch.feigns.constants;

/**
 * Created by mac on 15/8/28.
 */
public class ItemConstant {

    /**
     * 应用名称
     */
    public static final String SJES_API_ITEM = "sjes-api-item";

    /**
     * 单品状态
     */
    public class ProductStatusConstants {

        /**
         * 正常销售
         */
        public static final int NORMAL = 0;

        /**
         * 下架停售
         */
        public static final int UNSHELF = 1;

        /**
         * 未审核
         */
        public static final int UNAUDIT = 2;


    }

}
