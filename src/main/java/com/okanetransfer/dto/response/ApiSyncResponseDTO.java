package com.okanetransfer.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class ApiSyncResponseDTO {

    private int           totalProcessed;
    private int           updated;
    private int           unchanged;
    private int           failed;
    private LocalDateTime syncedAt;
    private String        apiSource;
    private List<String>  updatedCurrencies;
    private List<String>  errors;

    public ApiSyncResponseDTO() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private int           totalProcessed;
        private int           updated;
        private int           unchanged;
        private int           failed;
        private LocalDateTime syncedAt;
        private String        apiSource;
        private List<String>  updatedCurrencies;
        private List<String>  errors;

        public Builder totalProcessed(int v)
        { this.totalProcessed = v; return this; }
        public Builder updated(int v)
        { this.updated = v; return this; }
        public Builder unchanged(int v)
        { this.unchanged = v; return this; }
        public Builder failed(int v)
        { this.failed = v; return this; }
        public Builder syncedAt(LocalDateTime v)
        { this.syncedAt = v; return this; }
        public Builder apiSource(String v)
        { this.apiSource = v; return this; }
        public Builder updatedCurrencies(List<String> v)
        { this.updatedCurrencies = v; return this; }
        public Builder errors(List<String> v)
        { this.errors = v; return this; }

        public ApiSyncResponseDTO build() {
            ApiSyncResponseDTO d = new ApiSyncResponseDTO();
            d.totalProcessed    = totalProcessed;
            d.updated           = updated;
            d.unchanged         = unchanged;
            d.failed            = failed;
            d.syncedAt          = syncedAt != null
                    ? syncedAt : LocalDateTime.now();
            d.apiSource         = apiSource;
            d.updatedCurrencies = updatedCurrencies;
            d.errors            = errors;
            return d;
        }
    }

    public int getTotalProcessed() { return totalProcessed; }
    public void setTotalProcessed(int v) { this.totalProcessed = v; }
    public int getUpdated() { return updated; }
    public void setUpdated(int v) { this.updated = v; }
    public int getUnchanged() { return unchanged; }
    public void setUnchanged(int v) { this.unchanged = v; }
    public int getFailed() { return failed; }
    public void setFailed(int v) { this.failed = v; }
    public LocalDateTime getSyncedAt() { return syncedAt; }
    public void setSyncedAt(LocalDateTime v) { this.syncedAt = v; }
    public String getApiSource() { return apiSource; }
    public void setApiSource(String v) { this.apiSource = v; }
    public List<String> getUpdatedCurrencies()
    { return updatedCurrencies; }
    public void setUpdatedCurrencies(List<String> v)
    { this.updatedCurrencies = v; }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> v) { this.errors = v; }

    public String getSummary() {
        return updated + " devises mises à jour, "
                + unchanged + " inchangées"
                + (failed > 0 ? ", " + failed + " erreurs" : "");
    }
}