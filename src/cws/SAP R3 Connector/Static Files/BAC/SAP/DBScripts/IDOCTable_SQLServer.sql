if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[IDOCTable]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[IDOCTable]
GO

CREATE TABLE [dbo].[IDOCTABLE](
	[DIRECTION] [varchar](1) NOT NULL,
	[TID] [varchar](24) NOT NULL,
	[IDOCNUM] [varchar](24) NOT NULL,
	[CREATIONDATE] [datetime] NULL,
	[MESTYPE] [varchar](24) NOT NULL,
	[IDOCTYPE] [varchar](24) NOT NULL,
	[CIMTYPE] [varchar](24) NULL,
	[SENDERLS] [varchar](10) NULL,
	[RECEIVERLS] [varchar](10) NULL,
	[LOCALSTATUS] [varchar](24) NOT NULL,
	[ERRORTEXT] [text] NULL,
	[TARGETSYSTEM] [varchar](24) NULL,
	[DESTINATIONSTATUS] [varchar](2) NULL,
	[CONTROLRECORD] [text] NOT NULL,
	[DATARECORD] [text] NOT NULL,
	[SOAPNODEDN] [varchar](100) NOT NULL,
 CONSTRAINT [PK_IDOCNUM] PRIMARY KEY CLUSTERED 
(
	[IDOCNUM] ASC,
	[SOAPNODEDN] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
