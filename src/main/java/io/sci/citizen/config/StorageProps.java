package io.sci.citizen.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage")
class StorageProps {
    private String type;
    private Local local = new Local();
    private S3 s3 = new S3();

    public String getType() {
        return type;
    }

    public Local getLocal() {
        return  local;
    }

    public S3 getS3() {
        return s3;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public void setS3(S3 s3) {
        this.s3 = s3;
    }

    // getters/setters
    public static class Local {
        private String basePath;
        private String baseUrl;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }
    }
    public static class S3 {
        private String bucket;
        private String region;
        private String prefix = "uploads/";
        private Integer urlMinutes = 10;
        private String accessKey;
        private String secretKey;
        private String sessionToken;
        private String profile;

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public Integer getUrlMinutes() {
            return urlMinutes;
        }

        public void setUrlMinutes(Integer urlMinutes) {
            this.urlMinutes = urlMinutes;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getSessionToken() {
            return sessionToken;
        }

        public void setSessionToken(String sessionToken) {
            this.sessionToken = sessionToken;
        }

        public String getProfile() {
            return profile;
        }

        public void setProfile(String profile) {
            this.profile = profile;
        }
    }
}