CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    phone           VARCHAR(50),
    role            VARCHAR(20)  NOT NULL,
    address         VARCHAR(500),
    latitude        DOUBLE,
    longitude       DOUBLE,
    profile_photo   VARCHAR(500),
    is_verified     BOOLEAN      NOT NULL DEFAULT FALSE,
    is_approved     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      DATETIME
);

CREATE TABLE food_listings (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(255) NOT NULL,
    description       VARCHAR(2000),
    category          VARCHAR(100),
    quantity          INT,
    quantity_unit     VARCHAR(50),
    photo_url         VARCHAR(500),
    donor_id          BIGINT,
    donor_name        VARCHAR(255),
    donor_phone       VARCHAR(50),
    address           VARCHAR(500),
    latitude          DOUBLE,
    longitude         DOUBLE,
    pickup_start_time DATETIME,
    pickup_end_time   DATETIME,
    expiry_time       DATETIME,
    status            VARCHAR(20)  NOT NULL,
    created_at        DATETIME,
    version           BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT fk_food_listings_donor FOREIGN KEY (donor_id) REFERENCES users (id)
);

CREATE TABLE claims (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    listing_id      BIGINT       NOT NULL,
    volunteer_id    BIGINT,
    volunteer_name  VARCHAR(255),
    volunteer_phone VARCHAR(50),
    ngo_id          BIGINT,
    ngo_name        VARCHAR(255),
    status          VARCHAR(20)  NOT NULL,
    picked_up_at    DATETIME,
    delivered_at    DATETIME,
    created_at      DATETIME,
    CONSTRAINT fk_claims_listing FOREIGN KEY (listing_id) REFERENCES food_listings (id),
    CONSTRAINT fk_claims_volunteer FOREIGN KEY (volunteer_id) REFERENCES users (id),
    CONSTRAINT fk_claims_ngo FOREIGN KEY (ngo_id) REFERENCES users (id)
);

CREATE TABLE notifications (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT,
    user_name   VARCHAR(255),
    message     VARCHAR(1000),
    type        VARCHAR(30)  NOT NULL,
    is_read     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  DATETIME,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE reviews (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_id        BIGINT       NOT NULL,
    reviewer_id     BIGINT       NOT NULL,
    reviewer_name   VARCHAR(255),
    reviewee_id     BIGINT       NOT NULL,
    reviewee_name   VARCHAR(255),
    rating          INT          NOT NULL,
    comment         VARCHAR(2000),
    created_at      DATETIME,
    CONSTRAINT fk_reviews_claim FOREIGN KEY (claim_id) REFERENCES claims (id),
    CONSTRAINT fk_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES users (id),
    CONSTRAINT fk_reviews_reviewee FOREIGN KEY (reviewee_id) REFERENCES users (id)
);

CREATE INDEX idx_food_listings_status ON food_listings (status);
CREATE INDEX idx_food_listings_donor_id ON food_listings (donor_id);
CREATE INDEX idx_claims_volunteer_id ON claims (volunteer_id);
CREATE INDEX idx_claims_ngo_id ON claims (ngo_id);
CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_is_read ON notifications (is_read);
CREATE INDEX idx_reviews_reviewer_id ON reviews (reviewer_id);
CREATE INDEX idx_reviews_reviewee_id ON reviews (reviewee_id);
