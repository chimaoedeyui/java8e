package bluetooth;

import java.util.UUID;

/**
 * Created by jk on 2016/5/6 0006.
 */
public class AddressModel {

    private String macAddress=null;
    private String characteristicUuid=null;

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String  getCharacteristicUuid() {
        return characteristicUuid;
    }

    public void setCharacteristicUuid(String  characteristicUuid) {
        this.characteristicUuid = characteristicUuid;
    }

    public boolean isNULL(){
        return (macAddress.equals(null) ||
                macAddress==null ||
                characteristicUuid.equals(null) ||
                characteristicUuid==null);
    }


}
