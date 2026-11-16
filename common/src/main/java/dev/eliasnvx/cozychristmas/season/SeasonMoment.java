package dev.eliasnvx.cozychristmas.season;

/** Where a seasons mod's calendar stands: the sub-season and the day within it (0 = its first day). */
public record SeasonMoment(SubSeason subSeason, int day) {
}
