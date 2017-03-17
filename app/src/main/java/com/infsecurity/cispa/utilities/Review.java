package com.infsecurity.cispa.utilities;

/**
 * Created by subha on 3/1/2016.
 */
public class Review {


        String authorName;
        String authorURL;
        String rating;
        String reviewText;
        String profileURL;
        String timestamp;

        public String getRating() {
            return rating;
        }

        public void setRating(String rating) {
            this.rating = rating;
        }

        public String getAuthorName() {
            return authorName;
        }

        public void setAuthorName(String authorName) {
            this.authorName = authorName;
        }

        public String getAuthorURL() {
            return authorURL;
        }

        public void setAuthorURL(String authorURL) {
            this.authorURL = authorURL;
        }

        public String getProfileURL() {
            return profileURL;
        }

        public void setProfileURL(String profileURL) {
            this.profileURL = profileURL;
        }

        public String getReviewText() {
            return reviewText;
        }

        public void setReviewText(String reviewText) {
            this.reviewText = reviewText;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }




}
