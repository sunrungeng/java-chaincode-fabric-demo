/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.authentication;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.Objects;

@DataType()
public final class User {

    // username
    @Property()
    private final String belongto;

    // id
    @Property()
    private final String identity;

    // 上级
    @Property()
    private final String zone;

    @Property()
    private final int idCount;

    @Property()
    private final int deviceCount;

    // MPK
    @Property()
    private final String publicParameter;

    public String getBelongto() {
        return belongto;
    }

    public String getIdentity() {
        return identity;
    }

    public String getZone() {
        return zone;
    }

    public int getIdCount() {
        return idCount;
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    public String getPublicParameter() {
        return publicParameter;
    }

    public User(@JsonProperty("belongto") final String belongto, @JsonProperty("identity") final String identity,
                @JsonProperty("zone") final String zone, @JsonProperty("idCount") final int idCount,
                @JsonProperty("deviceCount") final int deviceCount, @JsonProperty("publicParameter") final String publicParameter) {
        this.belongto = belongto;
        this.identity = identity;
        this.zone = zone;
        this.idCount = idCount;
        this.deviceCount = deviceCount;
        this.publicParameter = publicParameter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return idCount == user.idCount &&
                deviceCount == user.deviceCount &&
                Objects.equals(belongto, user.belongto) &&
                Objects.equals(identity, user.identity) &&
                Objects.equals(zone, user.zone) &&
                Objects.equals(publicParameter, user.publicParameter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(belongto, identity, zone, idCount, deviceCount, publicParameter);
    }
}
