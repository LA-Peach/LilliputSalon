-----------------------------------------------------
-- Create Database
-----------------------------------------------------
IF DB_ID('LilliputSalon') IS NULL
    CREATE DATABASE LilliputSalon;
GO

USE LilliputSalon;
GO

-----------------------------------------------------
-- User Type
-----------------------------------------------------
CREATE TABLE [User_Type] (
    UserTypeID INT IDENTITY(1,1) PRIMARY KEY,
    TypeName VARCHAR(50) NOT NULL,
    TypeDescription VARCHAR(255)
);

-----------------------------------------------------
-- User
-----------------------------------------------------
CREATE TABLE [User] (
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    Email VARCHAR(255) NOT NULL UNIQUE,
    PasswordHash VARCHAR(255) NOT NULL,
    IsActive BIT NOT NULL DEFAULT 1,
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE()
);

-----------------------------------------------------
-- Profile
-----------------------------------------------------
CREATE TABLE [Profile] (
    ProfileID INT IDENTITY(1,1) PRIMARY KEY,
    UserID INT NOT NULL,
    UserTypeID INT NOT NULL,
    FirstName VARCHAR(100),
    LastName VARCHAR(100),
    Phone VARCHAR(20),
    HairType VARCHAR(50),
    HairLength VARCHAR(50),
    Preferences VARCHAR(255),
    IsActiveStylist BIT,
    FOREIGN KEY (UserID) REFERENCES [User](UserID) ON DELETE CASCADE,
    FOREIGN KEY (UserTypeID) REFERENCES [User_Type](UserTypeID)
);

-----------------------------------------------------
-- Stylist Notes
-----------------------------------------------------
CREATE TABLE [Stylist_Note] (
    StylistNoteID INT IDENTITY(1,1) PRIMARY KEY,
    ProfileID INT NOT NULL,
    StylistID INT NOT NULL,              -- present in schema map
    NoteDateTime DATETIME NOT NULL,
    NoteText VARCHAR(255),
    FOREIGN KEY (ProfileID) REFERENCES [Profile](ProfileID) ON DELETE CASCADE
);

-----------------------------------------------------
-- Availability
-----------------------------------------------------
CREATE TABLE [Availability] (
    AvailabilityID INT IDENTITY(1,1) PRIMARY KEY,
    UserID INT NOT NULL,
    WorkDate DATE NOT NULL,
    DayStartTime TIME NOT NULL,
    DayEndTime TIME NOT NULL,
    IsAvailable BIT NOT NULL,
    FOREIGN KEY (UserID) REFERENCES [User](UserID) ON DELETE CASCADE
);

-----------------------------------------------------
-- Break Time
-----------------------------------------------------
CREATE TABLE [BreakTime] (
    BreakID INT IDENTITY(1,1) PRIMARY KEY,
    AvailabilityID INT NOT NULL,
    BreakStartTime TIME NOT NULL,
    BreakEndTime TIME NOT NULL,
    BreakType VARCHAR(50),
    FOREIGN KEY (AvailabilityID) REFERENCES [Availability](AvailabilityID) ON DELETE CASCADE
);

-----------------------------------------------------
-- Business Hours
-----------------------------------------------------
CREATE TABLE [Business_Hours] (
    BusinessHoursID INT IDENTITY(1,1) PRIMARY KEY,
    DayOfWeek INT NOT NULL,
    OpenTime TIME,
    CloseTime TIME,
    IsClosed BIT NOT NULL
);

-----------------------------------------------------
-- Services
-----------------------------------------------------
CREATE TABLE [Service_Category] (
    ServiceCategoryID INT IDENTITY(1,1) PRIMARY KEY,
    CategoryName VARCHAR(100) NOT NULL,
    Description VARCHAR(255),
    DisplayOrder INT
);

CREATE TABLE [Service] (
    ServiceID INT IDENTITY(1,1) PRIMARY KEY,
    ServiceCategoryID INT NOT NULL,
    ServiceName VARCHAR(100) NOT NULL,
    ServiceDescription VARCHAR(255),
    BasePrice DECIMAL(10,2) NOT NULL,
    TypicalDurationMinutes INT NOT NULL,
    IsAvailable BIT,
    FOREIGN KEY (ServiceCategoryID) REFERENCES [Service_Category](ServiceCategoryID)
);

-----------------------------------------------------
-- Appointments
-----------------------------------------------------
CREATE TABLE [Appointment] (
    AppointmentID INT IDENTITY(1,1) PRIMARY KEY,
    CustomerID INT NOT NULL,
    StylistID INT NOT NULL,
    BusinessHoursID INT NOT NULL,
    ScheduledStartDateTime DATETIME NOT NULL,
    DurationMinutes INT NOT NULL,
    Status VARCHAR(50) NOT NULL,
    BaseAmount DECIMAL(10,2),
    DiscountAmount DECIMAL(10,2),
    TotalAmount DECIMAL(10,2),
    PointsEarned INT,
    IsCompleted BIT,
    FOREIGN KEY (CustomerID) REFERENCES [Profile](ProfileID),
    FOREIGN KEY (StylistID) REFERENCES [Profile](ProfileID),
    FOREIGN KEY (BusinessHoursID) REFERENCES [Business_Hours](BusinessHoursID)
);

-----------------------------------------------------
-- Appointment Services
-----------------------------------------------------
CREATE TABLE [Appointment_Service] (
    AppointmentServiceID INT IDENTITY(1,1) PRIMARY KEY,
    AppointmentID INT NOT NULL,
    ServiceID INT NOT NULL,
    ActualPrice DECIMAL(10,2) NOT NULL,
    ActualDurationMinutes INT NOT NULL,
    FOREIGN KEY (AppointmentID) REFERENCES [Appointment](AppointmentID) ON DELETE CASCADE,
    FOREIGN KEY (ServiceID) REFERENCES [Service](ServiceID)
);

-----------------------------------------------------
-- Appointment History
-----------------------------------------------------
CREATE TABLE [Appointment_History] (
    AppointmentHistoryID INT IDENTITY(1,1) PRIMARY KEY,
    AppointmentID INT NOT NULL,
    ModifiedByUserID INT NOT NULL,
    ChangedAt DATETIME NOT NULL,
    Action VARCHAR(50),
    OldStartDateTime DATETIME,
    NewStartDateTime DATETIME,
    Notes VARCHAR(255),
    FOREIGN KEY (AppointmentID) REFERENCES [Appointment](AppointmentID),
    FOREIGN KEY (ModifiedByUserID) REFERENCES [User](UserID)
);

-----------------------------------------------------
-- Appointment Rewards
-----------------------------------------------------
CREATE TABLE [Appointment_Reward] (
    AppointmentRewardID INT IDENTITY(1,1) PRIMARY KEY,
    AppointmentID INT NOT NULL,
    RewardID INT NOT NULL,
    PointsRedeemed INT NOT NULL,
    DiscountValue DECIMAL(10,2),
    FOREIGN KEY (AppointmentID) REFERENCES [Appointment](AppointmentID),
    FOREIGN KEY (RewardID) REFERENCES [Reward](RewardID)
);

-----------------------------------------------------
-- Rewards
-----------------------------------------------------
CREATE TABLE [Reward_Tier] (
    RewardTierID INT IDENTITY(1,1) PRIMARY KEY,
    TierName VARCHAR(100),
    PointsRequired INT NOT NULL,
    TierDescription VARCHAR(255),
    DisplayOrder INT
);

CREATE TABLE [Reward] (
    RewardID INT IDENTITY(1,1) PRIMARY KEY,
    RewardTierID INT NOT NULL,
    RewardName VARCHAR(100),
    RewardDescription VARCHAR(255),
    DollarValue DECIMAL(10,2),
    IsActive BIT,
    FOREIGN KEY (RewardTierID) REFERENCES [Reward_Tier](RewardTierID)
);

CREATE TABLE [Reward_Account] (
    RewardAccountID INT IDENTITY(1,1) PRIMARY KEY,
    ProfileID INT NOT NULL,
    CurrentPoints INT,
    LastEarnedDate DATE,
    LastRedeemedDate DATE,
    FOREIGN KEY (ProfileID) REFERENCES [Profile](ProfileID)
);

CREATE TABLE [Reward_Transaction] (
    RewardTransactionID INT IDENTITY(1,1) PRIMARY KEY,
    RewardAccountID INT NOT NULL,
    AppointmentID INT,
    PointsDelta INT NOT NULL,
    TransactionType VARCHAR(50),
    TransactionDateTime DATETIME NOT NULL,
    Description VARCHAR(255),
    FOREIGN KEY (RewardAccountID) REFERENCES [Reward_Account](RewardAccountID),
    FOREIGN KEY (AppointmentID) REFERENCES [Appointment](AppointmentID)
);

-----------------------------------------------------
-- Walk-In Queue
-----------------------------------------------------
CREATE TABLE [WalkIn] (
    WalkInID INT IDENTITY(1,1) PRIMARY KEY,
    CustomerID INT,
    RequestedServiceID INT,
    TimeEntered DATETIME NOT NULL,
    EstimatedWaitMinutes INT,
    AssignedStylistID INT,
    Status VARCHAR(50) NOT NULL,
    FOREIGN KEY (CustomerID) REFERENCES [Profile](ProfileID),
    FOREIGN KEY (RequestedServiceID) REFERENCES [Service](ServiceID),
    FOREIGN KEY (AssignedStylistID) REFERENCES [Profile](ProfileID)
);

-----------------------------------------------------
-- System Diagrams (default SQL Server)
-----------------------------------------------------
CREATE TABLE sysdiagrams (
    name NVARCHAR(128) NOT NULL,
    principal_id INT NOT NULL,
    diagram_id INT IDENTITY(1,1) PRIMARY KEY,
    version INT,
    definition VARBINARY(MAX)
);
