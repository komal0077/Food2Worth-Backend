-- Optimistic locking for claim status transitions (pickup/deliver), mirroring
-- the @Version already used on food_listings for claim-creation races.
ALTER TABLE claims ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Supports UserRepository.findByRole / GET /api/users/role/{role}
CREATE INDEX idx_users_role ON users (role);

-- Supports claim lookups by listing (e.g. "claims for this listing")
CREATE INDEX idx_claims_listing_id ON claims (listing_id);

-- Supports NotificationService.getUnreadNotifications, now scoped per user
CREATE INDEX idx_notifications_user_id_is_read ON notifications (user_id, is_read);

-- Supports ReviewService.createReview's duplicate-review guard
CREATE UNIQUE INDEX uq_reviews_claim_reviewer ON reviews (claim_id, reviewer_id);
