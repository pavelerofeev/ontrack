-- 31. Build links (#431)

CREATE TABLE BUILD_LINKS (
  ID            SERIAL  NOT NULL,
  BUILDID       INTEGER NOT NULL,
  TARGETBUILDID INTEGER NOT NULL,
  CONSTRAINT BUILD_LINKS_PK PRIMARY KEY (ID),
  CONSTRAINT BUILD_LINKS_UQ UNIQUE (BUILDID, TARGETBUILDID),
  CONSTRAINT BUILD_LINKS_FK_BUILD FOREIGN KEY (BUILDID) REFERENCES BUILDS (ID) ON DELETE CASCADE,
  CONSTRAINT BUILD_LINKS_FK_TARGETBUILDID FOREIGN KEY (TARGETBUILDID) REFERENCES BUILDS (ID) ON DELETE CASCADE
);