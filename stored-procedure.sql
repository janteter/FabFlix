USE moviedb;
DELIMITER //

CREATE PROCEDURE add_movie(
	IN movie_title varchar(100),
	IN movie_year int,
    IN movie_director varchar(100),
    IN movie_star varchar(100),
    IN star_birth_year int, 
    IN movie_genre varchar(32)
)
BEGIN
	DECLARE current_movie_id_max VARCHAR(10) DEFAULT "ZERO";
    DECLARE id_length INT unsigned DEFAULT 0;
	DECLARE movie_id_numerical_portion INT unsigned DEFAULT 0;
    DECLARE movie_id_alpha_portion VARCHAR(10) DEFAULT "ZERO";
    DECLARE new_id_numerical_portion VARCHAR(10) DEFAULT "ZERO";
    DECLARE potential_length INT unsigned DEFAULT 0;
    DECLARE new_movie_id VARCHAR(10) DEFAULT "ZERO";
    
    DECLARE current_star_id_max VARCHAR(10) DEFAULT "ZERO";
    DECLARE id_numerical_portion INT unsigned DEFAULT 0;
    DECLARE id_alpha_portion VARCHAR(10) DEFAULT "ZERO";
    DECLARE new_star_id VARCHAR(10) DEFAULT "ZERO";
    
    DECLARE found_genre_name VARCHAR(32) DEFAULT NULL;
    DECLARE found_star_name VARCHAR(100) DEFAULT NULL; 
    DECLARE existent_movie INT unsigned DEFAULT 0;
    
    DECLARE add_genre_to_table INT unsigned DEFAULT 0;
    DECLARE add_star_to_table VARCHAR(10) DEFAULT NULL;
    DECLARE generated_price DECIMAL(4,2) DEFAULT 0.00;
    DECLARE star_yob INT unsigned DEFAULT 0;
    
    
    SET existent_movie := (SELECT COUNT(*)
    FROM movies m 
    WHERE(m.title = movie_title and m.year = movie_year and m.director = movie_director));
    
    IF(existent_movie = 0) THEN
		-- new movie id parsing
		
        SET current_movie_id_max := (SELECT max(id) FROM movies);
        SET id_length:= (SELECT LENGTH(current_movie_id_max));
        
        SET movie_id_alpha_portion := (SELECT SUBSTRING(current_movie_id_max, 1, 2));
        SET movie_id_numerical_portion := (SELECT CAST(SUBSTRING(current_movie_id_max, 3) AS SIGNED));
        SET movie_id_numerical_portion := movie_id_numerical_portion + 1;
        SET new_id_numerical_portion := (SELECT CAST(movie_id_numerical_portion AS CHAR));
        
        SET potential_length := (SELECT LENGTH(CONCAT(movie_id_alpha_portion, new_id_numerical_portion)));
		IF(potential_length < id_length) THEN
			WHILE potential_length < id_length DO
				SET movie_id_alpha_portion := CONCAT(movie_id_alpha_portion, '0');
                SET potential_length := potential_length + 1;
			END WHILE;
		END IF;
		SET new_movie_id = CONCAT(movie_id_alpha_portion, new_id_numerical_portion);
		-- new movie id parsing^
        
        SET found_genre_name := (SELECT g.name FROM genres g WHERE g.name = movie_genre); 
        IF(ISNULL(found_genre_name)) THEN
			INSERT INTO genres (name)
            VALUES (movie_genre);
        END IF;
        
		SET found_star_name := (SELECT s.name FROM stars s WHERE s.name = movie_star); 
        IF(ISNULL(found_star_name)) THEN
			-- new star id parsing
			SET current_star_id_max := (SELECT max(id) FROM stars);
			SET id_length := (SELECT LENGTH(current_star_id_max));
			
			SET id_alpha_portion := (SELECT SUBSTRING(current_star_id_max, 1, 2));
			SET id_numerical_portion := (SELECT CAST(SUBSTRING(current_star_id_max, 3) AS SIGNED));
			SET id_numerical_portion = id_numerical_portion + 1;
			SET new_id_numerical_portion := (SELECT CAST(id_numerical_portion AS CHAR));
			
			SET potential_length := (SELECT LENGTH(CONCAT(id_alpha_portion, new_id_numerical_portion)));
			SET new_star_id = id_alpha_portion;
			IF(potential_length < id_length) THEN
				WHILE potential_length < id_length DO
					SET id_alpha_portion = CONCAT(id_alpha_portion, '0');
					SET potential_length = potential_length + 1;
				END WHILE;
			END IF;
			SET new_star_id = CONCAT(id_alpha_portion, new_id_numerical_portion);
			-- new star id parsing^
			
            SET star_yob := star_birth_year;
			INSERT INTO stars (id, name, birthYear) -- need to also add a new id, will figure that out in a moment
            VALUES (new_star_id, movie_star, star_yob);
        END IF;
        
        SET add_genre_to_table := (SELECT g.id FROM genres g WHERE g.name = movie_genre);
		SET add_star_to_table := (SELECT s.id FROM stars s WHERE s.name = movie_star); 
		SET generated_price := (SELECT RAND()*(100-10)+10);

		INSERT INTO movies (id, title, year, director, price)
        VALUES (new_movie_id, movie_title, movie_year, movie_director, generated_price);

		INSERT INTO genres_in_movies (genreID, movieID)
        VALUES (add_genre_to_table, new_movie_id);
        
        INSERT INTO stars_in_movies (starID, movieID)
        VALUES (add_star_to_table, new_movie_id);
        
        SELECT g.genreID, s.starID, m.id 
        FROM (movies m INNER JOIN stars_in_movies s on m.id = s.movieId INNER JOIN genres_in_movies g on m.id = g.movieId)
        WHERE m.id = new_movie_id;
	ELSE
		Select 'This movie already exists' as  'error'; -- will chnage this later, just for testing
    END IF;

END; //
DELIMITER ;