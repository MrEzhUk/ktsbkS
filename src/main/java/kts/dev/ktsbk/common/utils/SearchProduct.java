package kts.dev.ktsbk.common.utils;

import kts.dev.ktsbk.common.db.box.KtsBoxType;

import java.io.Serializable;

public class SearchProduct implements Serializable {
    long x, y, z;
    long maxRecommendationCount;
    long radius;
    long worldId;
    long serverId;
    long currencyId;
    String minecraftId;
    long minCount;
    double maxCostPerCount;
    int requiredBoxType;
    boolean sortedByCost = false;
    boolean sortedByNear = false;

    public SearchProduct(long x, long y, long z, long maxRecommendationCount) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.maxRecommendationCount = maxRecommendationCount;
    }
    public long getRadius() {
        return radius;
    }

    public void setRadius(long radius) {
        this.radius = radius;
    }

    public long getWorldId() {
        return worldId;
    }

    public void setWorldId(long worldId) {
        this.worldId = worldId;
    }

    public String getMinecraftId() {
        return minecraftId;
    }

    public void setMinecraftId(String minecraftId) {
        this.minecraftId = minecraftId;
    }

    public long getMinCount() {
        return minCount;
    }

    public void setMinCount(long minCount) {
        this.minCount = minCount;
    }

    public double getMaxBuyCostPerCount() {
        return maxCostPerCount;
    }

    public void setMaxCostPerCount(double maxCostPerCount) {
        this.maxCostPerCount = maxCostPerCount;
    }
    public long getMaxRecommendationCount() {
        return maxRecommendationCount;
    }

    public void setMaxRecommendationCount(long maxRecommendationCount) {
        this.maxRecommendationCount = maxRecommendationCount;
    }
    public void clear() {
        radius = 0;
        worldId = 0;
        serverId = 0;
        currencyId = 0;
        requiredBoxType = KtsBoxType.ALL;
        minecraftId = "";
        minCount = 0;
        maxCostPerCount = 0.0;
        sortedByCost = false;
        sortedByNear = false;

    }

    public void setX(long x) {
        this.x = x;
    }

    public void setY(long y) {
        this.y = y;
    }

    public void setZ(long z) {
        this.z = z;
    }

    public long getX() {
        return x;
    }

    public long getY() {
        return y;
    }

    public long getZ() {
        return z;
    }

    public long getServerId() {
        return serverId;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    public long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(long currencyId) {
        this.currencyId = currencyId;
    }

    public double getMaxCostPerCount() {
        return maxCostPerCount;
    }

    public int getRequiredBoxType() {
        return requiredBoxType;
    }

    public void setRequiredBoxType(int requiredBoxType) {
        this.requiredBoxType = requiredBoxType;
    }

    public boolean isSortedByCost() {
        return sortedByCost;
    }

    public boolean isSortedByNear() {
        return sortedByNear;
    }

    public void setSortedByCost(boolean sortedByCost) {
        this.sortedByCost = sortedByCost;
    }

    public void setSortedByNear(boolean sortedByNear) {
        this.sortedByNear = sortedByNear;
    }

    @Override
    public String toString() {
        return "SearchProduct{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", maxRecommendationCount=" + maxRecommendationCount +
                ", radius=" + radius +
                ", worldId=" + worldId +
                ", serverId=" + serverId +
                ", currencyId=" + currencyId +
                ", minecraftId='" + minecraftId + '\'' +
                ", minCount=" + minCount +
                ", maxCostPerCount=" + maxCostPerCount +
                ", requiredBoxType=" + requiredBoxType +
                ", sortedByCost=" + sortedByCost +
                ", sortedByNear=" + sortedByNear +
                '}';
    }
}
