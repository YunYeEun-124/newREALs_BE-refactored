ALTER TABLE likes
DROP CONSTRAINT likes_user_id_fk;

ALTER TABLE likes
ADD CONSTRAINT likes_user_id_fk
FOREIGN KEY (user_id) REFERENCES accounts (id) ON DELETE CASCADE;

ALTER TABLE likes
DROP CONSTRAINT likes_news_id_fk;

ALTER TABLE likes
ADD CONSTRAINT likes_news_id_fk
FOREIGN KEY (news_id) REFERENCES basenews (id) ON DELETE CASCADE;


