package util.web3j.abi;


import util.web3j.abi.datatypes.DynamicBytes;
import util.web3j.abi.datatypes.Fixed;
import util.web3j.abi.datatypes.Int;
import util.web3j.abi.datatypes.Type;
import util.web3j.abi.datatypes.Ufixed;
import util.web3j.abi.datatypes.Uint;
import util.web3j.abi.datatypes.Utf8String;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility functions.
 */
public class Utils {
    private Utils() {}


    static String getSimpleTypeName(Class<?> type) {
        String simpleName = type.getSimpleName().toLowerCase();

        if (type.equals(Uint.class) || type.equals(Int.class)
                || type.equals(Ufixed.class) || type.equals(Fixed.class)) {
            return simpleName + "256";
        } else if (type.equals(Utf8String.class)) {
            return "string";
        } else if (type.equals(DynamicBytes.class)) {
            return "bytes";
        } else {
            return simpleName;
        }
    }


    @SuppressWarnings("unchecked")
    public static List<TypeReference<Type>> convert(List<TypeReference<?>> input) {
        List<TypeReference<Type>> result = new ArrayList<>(input.size());
        for(TypeReference<?> reference : input){
            result.add((TypeReference<Type>)reference);
        }
//        result.addAll(input.stream()
//                .map(typeReference -> (TypeReference<Type>) typeReference)
//                .collect(Collectors.toList()));
        return result;
    }
}
