Feature: Google Search Functionality

  As a user
  I want to search on Google
  So that I can find relevant information

  Scenario: User searches for a topic and gets results
    Given User navigates to Google home page
    When User searches for "Selenium WebDriver"
    Then Search results should be displayed
    And Results should contain "Selenium"

  Scenario: User can perform multiple searches
    Given User navigates to Google home page
    When User searches for "Java Programming"
    Then Search results should be displayed
    And Results should contain "Java"

  Scenario: Search with special characters
    Given User navigates to Google home page
    When User searches for "Cucumber BDD Framework"
    Then Search results should be displayed
    And Results should contain "Cucumber"
