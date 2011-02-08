if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[IDOCTable]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[IDOCTable]
GO

CREATE TABLE [dbo].[IDOCTable] (
	[Direction] [varchar] (1)  NOT NULL ,
	[Tid] [varchar] (24)  NOT NULL ,
	[IDOCNum] [varchar] (24)  NOT NULL  CONSTRAINT pk_IDOCNum PRIMARY KEY,
	[CreationDate] [datetime] NOT NULL ,
	[MESType] [varchar] (24)  NOT NULL ,
	[IDOCType] [varchar] (24)  NOT NULL ,
	[CIMType] [varchar] (24)  NULL ,
	[SenderLS] [varchar] (10)  NULL ,
	[ReceiverLS] [varchar] (10)  NULL ,
	[LocalStatus] [varchar] (24)  NOT NULL ,
	[ErrorText] [text]  NULL ,
	[TargetSystem] [varchar] (24)  NULL ,
	[DestinationStatus] [varchar] (2)  NULL ,
	[ControlRecord] [text]  NOT NULL ,
	[DataRecord] [text]  NOT NULL 
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
