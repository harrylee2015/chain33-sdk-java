package cn.chain33.javasdk.model.abi.datatypes.generated;

import cn.chain33.javasdk.model.abi.datatypes.StaticArray;
import cn.chain33.javasdk.model.abi.datatypes.Type;

import java.util.List;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use cn.chain33.javasdk.model.codegen.AbiTypesGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 */
public class StaticArray2<T extends Type> extends StaticArray<T> {
    @Deprecated
    public StaticArray2(List<T> values) {
        super(2, values);
    }

    @Deprecated
    @SafeVarargs
    public StaticArray2(T... values) {
        super(2, values);
    }

    public StaticArray2(Class<T> type, List<T> values) {
        super(type, 2, values);
    }

    @SafeVarargs
    public StaticArray2(Class<T> type, T... values) {
        super(type, 2, values);
    }
}
