package sjes.elasticsearch.constants;

/**
 * Created by qinhailong on 15-12-7.
 */
public class Constants {

    /**
     * 应用名称
     */
    public static final String SJES_API_CATEGORY = "sjes-api-category";

    /**
     * 分类级别常量
     */
    public class CategoryGradeConstants {

        /**
         * 一级分类
         */
        public static final int GRADE_ONE = 1;

        /**
         * 二级分类
         */
        public static final int GRADE_TWO = 2;

        /**
         * 三级分类
         */
        public static final int GRADE_THREE = 3;
    }

    /**
     * 应用名称
     */
    public static final String SJES_API_ITEM = "sjes-api-item";

    /**
     * 应用名称
     */
    public static final String AXSH_API_ITEM = "ANXIAN-SJES-API-ITEM";

    public static final String SJES_API_ORDER="sjes-api-order";

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

    /**
     * 应用名称
     */
    public static final String SJES_API_SALE = "sjes-api-sale";
}
