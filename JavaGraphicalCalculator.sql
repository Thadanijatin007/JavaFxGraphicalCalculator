USE [master]
GO
/****** Object:  Database [JavaGraphicalCalculator]    Script Date: 08-04-2021 12:31:22 ******/
CREATE DATABASE [JavaGraphicalCalculator];
GO
ALTER DATABASE [JavaGraphicalCalculator] SET COMPATIBILITY_LEVEL = 150
GO
ALTER DATABASE [JavaGraphicalCalculator] SET QUERY_STORE = OFF
GO
USE [JavaGraphicalCalculator]
GO
/****** Object:  Table [dbo].[Configuration]    Script Date: 08-04-2021 12:31:22 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Configuration](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[ConfigurationKey] [varchar](128) NOT NULL,
	[ConfigurationValue] [varchar](256) NOT NULL,
 CONSTRAINT [PK_Configuration] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
SET IDENTITY_INSERT [dbo].[Configuration] ON 
GO
INSERT [dbo].[Configuration] ([Id], [ConfigurationKey], [ConfigurationValue]) VALUES (1, N'xmin', N'-10')
GO
INSERT [dbo].[Configuration] ([Id], [ConfigurationKey], [ConfigurationValue]) VALUES (2, N'xmax', N'10')
GO
INSERT [dbo].[Configuration] ([Id], [ConfigurationKey], [ConfigurationValue]) VALUES (5, N'function', N'cos(x)+2*sin(3*x)')
GO
SET IDENTITY_INSERT [dbo].[Configuration] OFF
GO
USE [master]
GO
ALTER DATABASE [JavaGraphicalCalculator] SET  READ_WRITE 
GO
