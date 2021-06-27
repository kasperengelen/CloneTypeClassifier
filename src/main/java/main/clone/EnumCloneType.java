package main.clone;

/**
 * Enum that lists the different clone types.
 */
public enum EnumCloneType
{
    /**
     * Exact clones.
     */
    TYPE_1,
    /**
     * Parameterized clones.
     */
    TYPE_2,
    /**
     * Clones with gaps.
     */
    TYPE_3,
    /**
     * False positives.
     */
    FP;

    /**
     * Given the name of the clone type: "T1", "T2", "T3", or "FP", this will give the corresponding enum instance.
     *
     * @param type_name The clone type name
     *
     * @throws IllegalArgumentException If the type name is not valid.
     */
    public static EnumCloneType fromNameInXMLFile(String type_name) throws IllegalArgumentException {
        switch (type_name) {
            case "T1":
                return EnumCloneType.TYPE_1;
            case "T2":
                return EnumCloneType.TYPE_2;
            case "T3":
                return EnumCloneType.TYPE_3;
            case "FP":
                return EnumCloneType.FP;
            default:
                throw new IllegalArgumentException("Invalid type: '" + type_name + "'");
        }
    }


    /**
     * Compare the two clone types and return the most strict one. If both are null, null is returned
     *
     * Example, Max(Type-1, Type-3) = Type-1.
     */
    public static EnumCloneType max(EnumCloneType a, EnumCloneType b)
    {
        if (a == EnumCloneType.TYPE_1 || b == EnumCloneType.TYPE_1) {
            // if either is TYPE1 => TYPE1
            return EnumCloneType.TYPE_1;
        } else if (a == EnumCloneType.TYPE_2 || b == EnumCloneType.TYPE_2) {
            // if either is TYPE2 => TYPE2
            return EnumCloneType.TYPE_2;
        } else if (a == EnumCloneType.TYPE_3 || b == EnumCloneType.TYPE_3) {
            // if either is TYPE3 => TYPE3
            return EnumCloneType.TYPE_3;
        } else if (a == EnumCloneType.FP || b == EnumCloneType.FP) {
            // if either is FP => FP
            return EnumCloneType.FP;
        }

        return null;
    }

    /**
     * Compare the two clone types and return the least strict one. If both are null, null is returned
     *
     * Example: Max(Type-3, Type-2) = Type-3
     */
    public static EnumCloneType min(EnumCloneType a, EnumCloneType b)
    {
        if(a == null || b == null) {
            // either is null => return null
            return null;
        } else if (a == EnumCloneType.FP || b == EnumCloneType.FP) {
            // either is FP => return FP
            return EnumCloneType.FP;
        } else if(a == EnumCloneType.TYPE_3 || b == EnumCloneType.TYPE_3) {
            // either is type 3 => return type 3
            return EnumCloneType.TYPE_3;
        } else if(a == EnumCloneType.TYPE_2 || b == EnumCloneType.TYPE_2) {
            // either is type 2 => return type 2
            return EnumCloneType.TYPE_2;
        } else {
            // both are type 1
            return EnumCloneType.TYPE_1;
        }
    }
}
