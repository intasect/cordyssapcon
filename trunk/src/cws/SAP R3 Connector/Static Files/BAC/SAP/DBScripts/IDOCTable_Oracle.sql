CREATE TABLE IDOCTable(
Direction varchar2(1) not null,
Tid varchar2(24) not null,
IDOCNum varchar2(24) not null,
CreationDate date not null,
MESType varchar2(24) not null,
CIMType varchar2(24) null,
SenderLS varchar2(10) null,
ReceiverLS varchar2(10) null,
LocalStatus varchar2(24) not null,
ErrorText varchar2(16) null,
TargetSystem varchar2(24)  null,
DestinationStatus varchar2(2) null,
ControlRecord clob not null,
DataRecord clob not null,
CONSTRAINT IDOCTable_pk PRIMARY KEY (IDOCNum)
);
