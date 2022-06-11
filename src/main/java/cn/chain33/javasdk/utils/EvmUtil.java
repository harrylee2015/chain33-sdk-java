package cn.chain33.javasdk.utils;

import cn.chain33.javasdk.model.enums.AddressType;
import cn.chain33.javasdk.model.enums.ChainID;
import cn.chain33.javasdk.model.enums.SignType;
import cn.chain33.javasdk.model.evm.Abi;
import cn.chain33.javasdk.model.evm.compiler.CompilationResult;
import cn.chain33.javasdk.model.evm.compiler.SolidityCompiler;
import cn.chain33.javasdk.model.protobuf.EvmService;
import cn.chain33.javasdk.model.protobuf.TransactionAllProtobuf;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.chain33.javasdk.model.evm.compiler.SolidityCompiler.Options.*;

public class EvmUtil {

    public static byte[] execer = "evm".getBytes();

    private static final long EVM_FEE = 1000000;

    /**
     * @param code       合约代码内容
     * @param note       注释
     * @param alias      合约别名
     * @param privateKey 签名私钥
     * @return hash，即合约名
     * @description 部署合约（联盟主链的情况下调用）,此方法后续不再维护，统一用下面带GAS参数的方法）
     */
    @Deprecated
    public static String createEvmContract(byte[] code, String note, String alias, String privateKey) {
        EvmService.EVMContractAction.Builder evmActionBuilder = EvmService.EVMContractAction.newBuilder();
        evmActionBuilder.setCode(ByteString.copyFrom(code));
        evmActionBuilder.setNote(note);
        evmActionBuilder.setAlias(alias);
        evmActionBuilder.setContractAddr(TransactionUtil.getToAddress(execer));

        EvmService.EVMContractAction evmContractAction = evmActionBuilder.build();

        String createTxWithoutSign = TransactionUtil.createTxWithoutSign(execer, evmContractAction.toByteArray(),
                TransactionUtil.DEFAULT_FEE, 0);
        byte[] fromHexString = HexUtil.fromHexString(createTxWithoutSign);
        TransactionAllProtobuf.Transaction parseFrom = null;
        try {
            parseFrom = TransactionAllProtobuf.Transaction.parseFrom(fromHexString);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        TransactionAllProtobuf.Transaction signProbuf = TransactionUtil.signProbuf(parseFrom, privateKey);
        String hexString = HexUtil.toHexString(signProbuf.toByteArray());
        return hexString;
    }

    /**
     * @param code       合约代码内容
     * @param note       注释
     * @param alias      合约别名
     * @param privateKey 签名私钥
     * @param paraName   平行链名称（如果是主链的情况，此参数填空）
     * @return hash，即合约名
     * @description 部署合约（平行链的情况下调用，要传paraName（平行链名称）） 此方法后续不再维护，统一用下面带GAS参数的方法）
     */
    @Deprecated
    public static String createEvmContract(byte[] code, String note, String alias, String privateKey, String paraName) {
        EvmService.EVMContractAction.Builder evmActionBuilder = EvmService.EVMContractAction.newBuilder();
        evmActionBuilder.setCode(ByteString.copyFrom(code));
        evmActionBuilder.setNote(note);
        evmActionBuilder.setAlias(alias);
        evmActionBuilder.setContractAddr(TransactionUtil.getToAddress((paraName + "evm").getBytes()));

        EvmService.EVMContractAction evmContractAction = evmActionBuilder.build();

        String createTxWithoutSign = TransactionUtil.createTxWithoutSign((paraName + "evm").getBytes(), evmContractAction.toByteArray(),
                EVM_FEE, 0);
        byte[] fromHexString = HexUtil.fromHexString(createTxWithoutSign);
        TransactionAllProtobuf.Transaction parseFrom = null;
        try {
            parseFrom = TransactionAllProtobuf.Transaction.parseFrom(fromHexString);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        TransactionAllProtobuf.Transaction signProbuf = TransactionUtil.signProbuf(parseFrom, privateKey);
        String hexString = HexUtil.toHexString(signProbuf.toByteArray());
        return hexString;
    }

    /**
     * @param code       合约代码内容
     * @param note       注释
     * @param alias      合约别名
     * @param privateKey 签名私钥
     * @param paraName   平行链名称（如果是主链的情况，此参数填空）
     * @param gas        gas费
     * @return hash，即合约名
     * @description 部署合约（平行链的情况下调用，要传paraName（平行链名称））
     */
    public static String createEvmContract(byte[] code, String note, String alias, String privateKey, String paraName, long gas) {
        EvmService.EVMContractAction.Builder evmActionBuilder = EvmService.EVMContractAction.newBuilder();
        evmActionBuilder.setCode(ByteString.copyFrom(code));
        evmActionBuilder.setNote(note);
        evmActionBuilder.setAlias(alias);
        evmActionBuilder.setContractAddr(TransactionUtil.getToAddress((paraName + "evm").getBytes()));

        EvmService.EVMContractAction evmContractAction = evmActionBuilder.build();
        long fee = 0L;
        // 以防用户乱填GAS，导致交易执行不过，设置一个最小的GAS费
        if (gas < EVM_FEE) {
            fee = EVM_FEE;
        } else {
            fee = gas + 100000L;
        }

        String createTxWithoutSign = TransactionUtil.createTxWithoutSign((paraName + "evm").getBytes(), evmContractAction.toByteArray(),
                fee, 0);
        byte[] fromHexString = HexUtil.fromHexString(createTxWithoutSign);
        TransactionAllProtobuf.Transaction parseFrom = null;
        try {
            parseFrom = TransactionAllProtobuf.Transaction.parseFrom(fromHexString);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        TransactionAllProtobuf.Transaction signProbuf = TransactionUtil.signProbuf(parseFrom, privateKey);
        String hexString = HexUtil.toHexString(signProbuf.toByteArray());
        return hexString;
    }

    /**
     * 构造创建EVM合约交易
     *
     * @param code     合约代码内容
     * @param note     注释
     * @param alias    合约别名
     * @param paraName 平行链名称（如果是主链的情况，此参数填空）
     * @return
     */
    public static String getCreateEvmEncode(byte[] code, String note, String alias, String paraName) {
        EvmService.EVMContractAction.Builder evmActionBuilder = EvmService.EVMContractAction.newBuilder();
        evmActionBuilder.setCode(ByteString.copyFrom(code));
        evmActionBuilder.setNote(note);
        evmActionBuilder.setAlias(alias);
        evmActionBuilder.setContractAddr(TransactionUtil.getToAddress((paraName + "evm").getBytes()));

        EvmService.EVMContractAction evmContractAction = evmActionBuilder.build();

        String createTxWithoutSign = TransactionUtil.createTxWithoutSign((paraName + "evm").getBytes(), evmContractAction.toByteArray(),
                EVM_FEE, 0);
        return createTxWithoutSign;
    }

    /**
     * @param parameter  合约代码内容
     * @param note       注释
     * @param amount     转账金额
     * @param privateKey 签名私钥
     * @return hash
     * @description 调用合约（平行链的情况下调用，要传paraName（平行链名称））
     */
    @Deprecated
    public static String callEvmContract(byte[] parameter, String note, long amount, String contractAddr, String privateKey, String paraName) {
        EvmService.EVMContractAction.Builder evmActionBuilder = EvmService.EVMContractAction.newBuilder();
        evmActionBuilder.setPara(ByteString.copyFrom(parameter));
        evmActionBuilder.setNote(note);
        evmActionBuilder.setAmount(amount);
        evmActionBuilder.setContractAddr(contractAddr);
        EvmService.EVMContractAction evmContractAction = evmActionBuilder.build();

        String createTxWithoutSign = TransactionUtil.createTxWithoutSign((paraName + "evm").getBytes(), evmContractAction.toByteArray(),
                EVM_FEE, 0);
        byte[] fromHexString = HexUtil.fromHexString(createTxWithoutSign);
        TransactionAllProtobuf.Transaction parseFrom = null;
        try {
            parseFrom = TransactionAllProtobuf.Transaction.parseFrom(fromHexString);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        TransactionAllProtobuf.Transaction signProbuf = TransactionUtil.signProbuf(parseFrom, privateKey);
        return HexUtil.toHexString(signProbuf.toByteArray());
    }


    /**
     * @param parameter  合约代码内容
     * @param note       注释
     * @param amount     转账金额
     * @param privateKey 签名私钥
     * @return hash
     * @description 调用合约（平行链的情况下调用，要传paraName（平行链名称））
     */
    public static String callEvmContract(byte[] parameter, String note, long amount, String contractAddr, String privateKey, String paraName, long gas) {
        EvmService.EVMContractAction.Builder evmActionBuilder = EvmService.EVMContractAction.newBuilder();
        evmActionBuilder.setPara(ByteString.copyFrom(parameter));
        evmActionBuilder.setNote(note);
        evmActionBuilder.setAmount(amount);
        evmActionBuilder.setContractAddr(contractAddr);
        EvmService.EVMContractAction evmContractAction = evmActionBuilder.build();
        long fee = 0L;
        if (gas < EVM_FEE) {
            fee = EVM_FEE;
        } else {
            fee = gas + 100000L;
        }

        String createTxWithoutSign = TransactionUtil.createTxWithoutSign((paraName + "evm").getBytes(), evmContractAction.toByteArray(),
                fee, 0);
        byte[] fromHexString = HexUtil.fromHexString(createTxWithoutSign);
        TransactionAllProtobuf.Transaction parseFrom = null;
        try {
            parseFrom = TransactionAllProtobuf.Transaction.parseFrom(fromHexString);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        TransactionAllProtobuf.Transaction signProbuf = TransactionUtil.signProbuf(parseFrom, privateKey);
        return HexUtil.toHexString(signProbuf.toByteArray());
    }


    /**
     * @param parameter 合约代码内容
     * @param note      注释
     * @param amount    转账金额
     * @return hash
     * @description 调用合约（平行链的情况下调用，要传paraName（平行链名称））
     */
    public static String getCallEvmEncode(byte[] parameter, String note, long amount, String contractAddr, String paraName) {
        EvmService.EVMContractAction.Builder evmActionBuilder = EvmService.EVMContractAction.newBuilder();
        evmActionBuilder.setPara(ByteString.copyFrom(parameter));
        evmActionBuilder.setNote(note);
        evmActionBuilder.setAmount(amount);
        evmActionBuilder.setContractAddr(contractAddr);
        EvmService.EVMContractAction evmContractAction = evmActionBuilder.build();

        String createTxWithoutSign = TransactionUtil.createTxWithoutSign((paraName + "evm").getBytes(), evmContractAction.toByteArray(),
                EVM_FEE, 0);
        return createTxWithoutSign;
    }

    /**
     * @param code       合约代码内容
     * @param note       注释
     * @param alias      合约别名
     * @param privateKey 签名私钥
     * @return hash，即合约名
     * @description 部署合约（平行链采用代扣的情况下调用）
     */
    public static String createEvmContractWithhold(byte[] code, String note, String alias, String privateKey, String execer, String contranctAddress, long gas) {
        EvmService.EVMContractAction.Builder evmActionBuilder = EvmService.EVMContractAction.newBuilder();
        evmActionBuilder.setCode(ByteString.copyFrom(code));
        evmActionBuilder.setNote(note);
        evmActionBuilder.setAlias(alias);
        evmActionBuilder.setContractAddr(contranctAddress);
        EvmService.EVMContractAction evmContractAction = evmActionBuilder.build();

        long fee = 0L;
        if (gas < EVM_FEE) {
            fee = EVM_FEE;
        } else {
            fee = gas + 100000L;
        }

        String createTransferTx = TransactionUtil.createTransferTx(privateKey, contranctAddress, execer, evmContractAction.toByteArray(), fee);

        return createTransferTx;
    }


    /**
     * @param parameter
     * @param note
     * @param amount
     * @param privateKey
     * @param contractAddress
     * @return
     * @description 调用合约（平行链采用代扣的情况下调用）
     */
    public static String callEvmContractWithhold(byte[] parameter, String note, long amount, String exec, String privateKey, String contractAddress) {
        EvmService.EVMContractAction.Builder evmActionBuilder = EvmService.EVMContractAction.newBuilder();
        evmActionBuilder.setPara(ByteString.copyFrom(parameter));
        evmActionBuilder.setNote(note);
        evmActionBuilder.setAmount(amount);
        evmActionBuilder.setContractAddr(contractAddress);
        EvmService.EVMContractAction evmContractAction = evmActionBuilder.build();

        String createTransferTx = TransactionUtil.createTransferTx(privateKey, TransactionUtil.getToAddress(exec.getBytes()), exec, evmContractAction.toByteArray(), EVM_FEE);

        return createTransferTx;
    }

    /**
     * @param parameter
     * @param note
     * @param amount
     * @param privateKey
     * @param contractAddress
     * @return
     * @description 调用合约（平行链采用代扣的情况下调用）
     */
    public static String callEvmContractWithholdByGas(byte[] parameter, String note, long amount, String exec, String privateKey, String contractAddress, long gas) {
        EvmService.EVMContractAction.Builder evmActionBuilder = EvmService.EVMContractAction.newBuilder();
        evmActionBuilder.setPara(ByteString.copyFrom(parameter));
        evmActionBuilder.setNote(note);
        evmActionBuilder.setAmount(amount);
        evmActionBuilder.setContractAddr(contractAddress);
        EvmService.EVMContractAction evmContractAction = evmActionBuilder.build();

        long fee = 0L;
        if (gas < EVM_FEE) {
            fee = EVM_FEE;
        } else {
            fee = gas + 100000L;
        }

        String createTransferTx = TransactionUtil.createTransferTx(privateKey, TransactionUtil.getToAddress(exec.getBytes()), exec, evmContractAction.toByteArray(), fee);

        return createTransferTx;
    }


    public static SolidityCompiler.Result compileContract(byte[] code, String version) throws IOException {
        return SolidityCompiler.compile(code, version, true, ABI, BIN, INTERFACE, METADATA);
    }

    public static CompilationResult.ContractMetadata paserCompileResult(SolidityCompiler.Result result, String contractName) throws IOException {
        CompilationResult compilationResult = CompilationResult.parse(result.getOutput());
        return compilationResult.getContract(contractName);
    }

    public static byte[] encodeParameter(String abiStr, String funcName, Object... params) throws Exception {
        try {
            Abi abiObj = Abi.fromJson(abiStr);
            Abi.Function func = abiObj.findFunction(s -> s.name.equals(funcName));
            return func.encode(params);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public static byte[] encodeContructor(String abiStr, Object... params) throws Exception {
        try {
            Abi abiObj = Abi.fromJson(abiStr);
            Abi.Constructor constructor = abiObj.findConstructor();
            return constructor.encode(params);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public static List<?> decodeOutput(String abiStr, String funcName, JSONObject output) {
        String resultArray = output.getString("rawData");

        Abi abiObj = Abi.fromJson(abiStr);
        Abi.Function func = abiObj.findFunction(s -> s.name.equals(funcName));

        return func.decodeResult(HexUtil.fromHexString(resultArray));
    }

    public static String getContractAddr(JSONObject result) {
        String resultStr = result.toJSONString();

        String pattern = "\"contractAddr\":\"(\\w+)\"";
        Pattern r = Pattern.compile(pattern);

        Matcher m = r.matcher(resultStr);

        if (m.find()) {
            String[] splits = StringUtils.split(m.group(0), ":");
            if (splits.length == 2) {
                return StringUtils.strip(splits[1], "\"");
            }
        }

        return "";
    }


    /**
     * @param codeStr     合约代码内容
     * @param constructorEncode  编码好后的构造参数
     * @param note        注释
     * @param alias       合约别名
     * @param privateKey  签名私钥
     * @param signType    签名类型 目前支持secep256k1,ETH_secep256k1
     * @param addressType 地址类型
     * @param chainID     链ID
     * @param paraName    平行链名称（如果是主链的情况，此参数填空）
     * @param gas         gas费
     * @return hash，即合约名
     * @description 新增一键部署合约方法（平行链的情况下调用，要传paraName（平行链名称））
     */
    public static String createEvmContract(String codeStr, String constructorEncode, String note, String alias, String privateKey, SignType signType, AddressType addressType, int chainID, String paraName, long gas) throws Exception {
        byte[] code;
        byte[] bytes = new byte[0];
        if (constructorEncode != null) {
            bytes=HexUtil.fromHexString(constructorEncode);
        }
        code = ByteUtil.merge(HexUtil.fromHexString(codeStr), bytes);
        EvmService.EVMContractAction.Builder evmActionBuilder = EvmService.EVMContractAction.newBuilder();
        evmActionBuilder.setCode(ByteString.copyFrom(code));
        evmActionBuilder.setNote(note);
        evmActionBuilder.setAlias(alias);
        evmActionBuilder.setContractAddr(TransactionUtil.getToAddress((paraName + "evm").getBytes(), addressType));

        EvmService.EVMContractAction evmContractAction = evmActionBuilder.build();
        long fee = 0L;
        // 以防用户乱填GAS，导致交易执行不过，设置一个最小的GAS费
        if (gas < EVM_FEE) {
            fee = EVM_FEE;
        } else {
            fee = gas + 100000L;
        }

        String createTxWithoutSign = TransactionUtil.createTxWithoutSign((paraName + "evm").getBytes(), evmContractAction.toByteArray(), addressType, chainID,
                fee, 0);
        byte[] fromHexString = HexUtil.fromHexString(createTxWithoutSign);
        TransactionAllProtobuf.Transaction parseFrom = null;
        try {
            parseFrom = TransactionAllProtobuf.Transaction.parseFrom(fromHexString);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        TransactionAllProtobuf.Transaction signProbuf = TransactionUtil.signProtobuf(parseFrom, privateKey, signType);
        String hexString = HexUtil.toHexString(signProbuf.toByteArray());
        return hexString;
    }

    /**
     * YCC中构建evm合约交易
     * @param codeStr
     * @param constructorEncode 编码后的构造函数
     * @param note
     * @param alias
     * @param privateKey
     * @param paraName
     * @param gas
     * @return
     * @throws Exception
     */
    public static String createEvmContractForYCC(String codeStr, String constructorEncode, String note, String alias, String privateKey, String paraName, long gas) throws Exception {
       return createEvmContract(codeStr,constructorEncode,note,alias,privateKey,SignType.ETH_SECP256K1,AddressType.ETH_ADDRESS, ChainID.YCC.getID(),paraName,gas);
    }

    /**
     * 新增通用的call evm合约方法（平行链的情况下调用，要传paraName（平行链名称））
     * @param functionEncode 编码后的调用函数
     * @param contractAddr
     * @param note
     * @param amount
     * @param privateKey
     * @param signType
     * @param addressType
     * @param chainID
     * @param paraName
     * @param gas
     * @return
     */
    public static String callEvmContract(String functionEncode, String contractAddr, String note, long amount, String privateKey, SignType signType, AddressType addressType, int chainID, String paraName, long gas) throws Exception {
        EvmService.EVMContractAction.Builder evmActionBuilder = EvmService.EVMContractAction.newBuilder();
        evmActionBuilder.setPara(ByteString.copyFrom(HexUtil.fromHexString(functionEncode)));
        evmActionBuilder.setNote(note);
        evmActionBuilder.setAmount(amount);
        evmActionBuilder.setContractAddr(contractAddr);
        EvmService.EVMContractAction evmContractAction = evmActionBuilder.build();
        long fee = 0L;
        if (gas < EVM_FEE) {
            fee = EVM_FEE;
        } else {
            fee = gas + 100000L;
        }

        String createTxWithoutSign = TransactionUtil.createTxWithoutSign((paraName + "evm").getBytes(), evmContractAction.toByteArray(), addressType, chainID,
                fee, 0);
        byte[] fromHexString = HexUtil.fromHexString(createTxWithoutSign);
        TransactionAllProtobuf.Transaction parseFrom = null;
        try {
            parseFrom = TransactionAllProtobuf.Transaction.parseFrom(fromHexString);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        TransactionAllProtobuf.Transaction signProbuf = TransactionUtil.signProtobuf(parseFrom, privateKey, signType);
        String hexString = HexUtil.toHexString(signProbuf.toByteArray());
        return hexString;
    }

    /**
     * YCC中构建调用evm合约交易
     * @param functionEncode 编码后的调用函数
     * @param contractAddr
     * @param note
     * @param amount
     * @param privateKey
     * @param paraName
     * @param gas
     * @return
     * @throws Exception
     */
    public static String callEvmContractForYCC(String functionEncode, String contractAddr, String note, long amount, String privateKey, String paraName, long gas) throws Exception {
        return callEvmContract(functionEncode,contractAddr,note,amount,privateKey,SignType.ETH_SECP256K1,AddressType.ETH_ADDRESS, ChainID.YCC.getID(),paraName,gas);
    }
}
