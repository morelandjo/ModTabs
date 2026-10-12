package vodmordia.modtabs.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class ModTabsConfig extends MidnightConfig {

    public static class CommentText {}

    // ========== MOD TABS SETTINGS ==========

    @Comment(category = "1_modTabsSettings") public static CommentText inventory;

    @Entry(category = "1_modTabsSettings")
    public static boolean stickyInventoryTab = true;

    // Featured tabs sort ahead of the others and render at featuredTabScale percent.
    @Entry(category = "1_modTabsSettings", min = 50, max = 300)
    public static int featuredTabScale = 125;

    @Entry(category = "1_modTabsSettings")
    public static boolean inventoryTabEnabled = true;

    @Entry(category = "1_modTabsSettings")
    public static int inventoryTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static boolean inventoryTabFeatured = false;

    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility inventoryTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacer1;

    @Comment(category = "1_modTabsSettings") public static CommentText advancements;

    @Entry(category = "1_modTabsSettings")
    public static boolean advancementsTabEnabled = true;

    @Entry(category = "1_modTabsSettings")
    public static int advancementsTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static boolean advancementsTabFeatured = false;

    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility advancementsTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacer2;

    @Comment(category = "1_modTabsSettings") public static CommentText ftbQuests;

    @Entry(category = "1_modTabsSettings")
    public static boolean ftbQuestsTabEnabled = true;

    @Entry(category = "1_modTabsSettings")
    public static int ftbQuestsTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static boolean ftbQuestsTabFeatured = false;

    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility ftbQuestsTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacer3;

    @Comment(category = "1_modTabsSettings") public static CommentText ftbTeams;

    @Entry(category = "1_modTabsSettings")
    public static boolean ftbTeamsTabEnabled = true;

    @Entry(category = "1_modTabsSettings")
    public static int ftbTeamsTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static boolean ftbTeamsTabFeatured = false;

    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility ftbTeamsTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacer4;

    @Comment(category = "1_modTabsSettings") public static CommentText mapAtlases;

    @Entry(category = "1_modTabsSettings")
    public static boolean mapAtlasesTabEnabled = true;

    @Entry(category = "1_modTabsSettings")
    public static int mapAtlasesTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static boolean mapAtlasesTabFeatured = false;

    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility mapAtlasesTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacer5;

    @Comment(category = "1_modTabsSettings") public static CommentText xaerosMap;

    @Entry(category = "1_modTabsSettings")
    public static boolean xaerosMapTabEnabled = true;

    @Entry(category = "1_modTabsSettings")
    public static int xaerosMapTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static boolean xaerosMapTabFeatured = false;

    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility xaerosMapTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacer6;

    @Comment(category = "1_modTabsSettings") public static CommentText pufferfishSkills;

    @Entry(category = "1_modTabsSettings")
    public static boolean pufferfishSkillsTabEnabled = true;

    @Entry(category = "1_modTabsSettings")
    public static int pufferfishSkillsTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static boolean pufferfishSkillsTabFeatured = false;

    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility pufferfishSkillsTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacer7;

    @Comment(category = "1_modTabsSettings") public static CommentText sophisticatedBackpacks;

    @Entry(category = "1_modTabsSettings")
    public static boolean sophisticatedBackpacksTabEnabled = true;

    @Entry(category = "1_modTabsSettings")
    public static int sophisticatedBackpacksTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static boolean sophisticatedBackpacksTabFeatured = false;

    @Entry(category = "1_modTabsSettings")
    public static BackpackSlot sophisticatedBackpacksPreferredSlot = BackpackSlot.DEFAULT;

    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility sophisticatedBackpacksTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacer8;

    @Comment(category = "1_modTabsSettings") public static CommentText travelersBackpack;

    @Entry(category = "1_modTabsSettings")
    public static boolean travelersBackpackTabEnabled = true;

    @Entry(category = "1_modTabsSettings")
    public static int travelersBackpackTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static boolean travelersBackpackTabFeatured = false;

    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility travelersBackpackTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacerOrigins;
    @Comment(category = "1_modTabsSettings") public static CommentText origins;
    @Entry(category = "1_modTabsSettings") public static boolean originsTabEnabled = true;
    @Entry(category = "1_modTabsSettings") public static int originsTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static boolean originsTabFeatured = false;
    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility originsTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacerBiologyDictionary;
    @Comment(category = "1_modTabsSettings") public static CommentText biologyDictionary;
    @Entry(category = "1_modTabsSettings") public static boolean biologyDictionaryTabEnabled = true;
    @Entry(category = "1_modTabsSettings") public static int biologyDictionaryTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static boolean biologyDictionaryTabFeatured = false;
    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility biologyDictionaryTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacerReliableBackpacks;
    @Comment(category = "1_modTabsSettings") public static CommentText reliableBackpacks;
    @Entry(category = "1_modTabsSettings") public static boolean reliableBackpacksTabEnabled = true;
    @Entry(category = "1_modTabsSettings") public static int reliableBackpacksTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static boolean reliableBackpacksTabFeatured = false;
    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility reliableBackpacksTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacerBulking;
    @Comment(category = "1_modTabsSettings") public static CommentText bulking;
    @Entry(category = "1_modTabsSettings") public static boolean bulkingTabEnabled = true;
    @Entry(category = "1_modTabsSettings") public static int bulkingTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static boolean bulkingTabFeatured = false;
    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility bulkingTabDisplayVisibility = TabDisplayVisibility.YES;
    @Comment(category = "1_modTabsSettings") public static CommentText spacerCobblemon;
    @Comment(category = "1_modTabsSettings") public static CommentText cobblemon;
    @Entry(category = "1_modTabsSettings") public static boolean cobblemonTabEnabled = true;
    @Entry(category = "1_modTabsSettings") public static int cobblemonTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static boolean cobblemonTabFeatured = false;
    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility cobblemonTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacerAether;
    @Comment(category = "1_modTabsSettings") public static CommentText aether;
    @Entry(category = "1_modTabsSettings") public static boolean aetherTabEnabled = true;
    @Entry(category = "1_modTabsSettings") public static int aetherTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static boolean aetherTabFeatured = false;
    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility aetherTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacerCompletionistsIndex;
    @Comment(category = "1_modTabsSettings") public static CommentText completionistsIndex;
    @Entry(category = "1_modTabsSettings") public static boolean completionistsIndexTabEnabled = true;
    @Entry(category = "1_modTabsSettings") public static int completionistsIndexTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static boolean completionistsIndexTabFeatured = false;
    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility completionistsIndexTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacerFieldGuide;
    @Comment(category = "1_modTabsSettings") public static CommentText fieldGuide;
    @Entry(category = "1_modTabsSettings") public static boolean fieldGuideTabEnabled = true;
    @Entry(category = "1_modTabsSettings") public static int fieldGuideTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static boolean fieldGuideTabFeatured = false;
    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility fieldGuideTabDisplayVisibility = TabDisplayVisibility.YES;
    @Comment(category = "1_modTabsSettings") public static CommentText spacerSdmShop;
    @Comment(category = "1_modTabsSettings") public static CommentText sdmShop;
    @Entry(category = "1_modTabsSettings") public static boolean sdmShopTabEnabled = true;
    @Entry(category = "1_modTabsSettings") public static int sdmShopTabOrder = 0;
    @Entry(category = "1_modTabsSettings") public static boolean sdmShopTabFeatured = false;

    @Comment(category = "1_modTabsSettings") public static CommentText spacerJobsPlus;
    @Comment(category = "1_modTabsSettings") public static CommentText jobsPlus;
    @Entry(category = "1_modTabsSettings") public static boolean jobsPlusTabEnabled = true;
    @Entry(category = "1_modTabsSettings") public static int jobsPlusTabOrder = 0;
    @Entry(category = "1_modTabsSettings") public static boolean jobsPlusTabFeatured = false;
    @Entry(category = "1_modTabsSettings") public static TabDisplayVisibility jobsPlusTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacerQuestLog;
    @Comment(category = "1_modTabsSettings") public static CommentText questLog;
    @Entry(category = "1_modTabsSettings") public static boolean questLogTabEnabled = true;
    @Entry(category = "1_modTabsSettings") public static int questLogTabOrder = 0;
    @Entry(category = "1_modTabsSettings") public static boolean questLogTabFeatured = false;
    @Entry(category = "1_modTabsSettings") public static TabDisplayVisibility questLogTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacerModonomicon;
    @Comment(category = "1_modTabsSettings") public static CommentText modonomicon;
    @Entry(category = "1_modTabsSettings") public static boolean modonomiconTabEnabled = true;
    @Entry(category = "1_modTabsSettings") public static int modonomiconTabOrder = 0;
    @Entry(category = "1_modTabsSettings") public static boolean modonomiconTabFeatured = false;
    @Entry(category = "1_modTabsSettings") public static TabDisplayVisibility modonomiconTabDisplayVisibility = TabDisplayVisibility.YES;

    // ========== CUSTOM TAB ==========

    @Comment(category = "2_customTab") public static CommentText spacer9;

    @Comment(category = "2_customTab") public static CommentText customTabs;

    @Entry(category = "2_customTab")
    public static boolean customTabsEnabled = true;

    @Entry(category = "2_customTab")
    public static boolean customTabsDebugLogging = false;
}
