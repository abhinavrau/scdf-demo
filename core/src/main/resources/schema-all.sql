CREATE TABLE IF NOT EXISTS Demo_FileImport (
    person_id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(20),
    last_name VARCHAR(20),
    PRIMARY KEY (person_id)
);

CREATE TABLE IF NOT EXISTS Demo_Case (
    person_id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(20),
    last_name VARCHAR(20),
    PRIMARY KEY (person_id)
);

CREATE TABLE IF NOT EXISTS Demo_Reverse (
    person_id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(20),
    last_name VARCHAR(20),
    PRIMARY KEY (person_id)
);

