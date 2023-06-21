DROP DATABASE IF EXISTS sl190204

CREATE DATABASE sl190204
go
USE sl190204
go

CREATE TABLE [Artikal]
( 
	[idPro]              integer  NULL ,
	[idA]                integer  IDENTITY  NOT NULL ,
	[cena]               integer  NULL ,
	[kolicina]           integer  NULL ,
	[naziv]              varchar(100)  NOT NULL 
)
go

CREATE TABLE [Grad]
( 
	[idG]                integer  IDENTITY  NOT NULL ,
	[naziv]              char(100)  NOT NULL 
)
go

CREATE TABLE [Kupac]
( 
	[idG]                integer  NULL ,
	[idK]                integer  IDENTITY  NOT NULL ,
	[ime]                varchar(100)  NULL ,
	[novac]              decimal(10,3)  NULL 
)
go

CREATE TABLE [Linija]
( 
	[razdaljinaUDanima]  integer  NULL ,
	[idG1]               integer  NOT NULL ,
	[idG2]               integer  NOT NULL ,
	[idL]                integer  IDENTITY  NOT NULL 
)
go

CREATE TABLE [Porudzbina]
( 
	[idPor]              integer  IDENTITY  NOT NULL ,
	[stanje]             varchar(100)  NULL ,
	[cenaUkupno]         decimal(10,3)  NULL ,
	[datumStigloNajbliziGrad] datetime  NULL ,
	[datumPoslato]       datetime  NULL ,
	[datumStigloDoKupca] datetime  NULL ,
	[idK]                integer  NULL ,
	[cenaSaPopustomUkupno] decimal(10,3)  NULL ,
	[idGNajblizi]        integer  NULL ,
	[idG]                integer  NULL 
)
go

CREATE TABLE [Prodavnica]
( 
	[idPro]              integer  IDENTITY  NOT NULL ,
	[idG]                integer  NULL ,
	[naziv]              varchar(100)  NOT NULL ,
	[popust]             integer  NULL ,
	[profit]             decimal(10,3)  NULL 
)
go

CREATE TABLE [Stavka]
( 
	[kolicina]           integer  NULL ,
	[idA]                integer  NULL ,
	[idPor]              integer  NULL ,
	[idS]                integer  IDENTITY  NOT NULL 
)
go

CREATE TABLE [Transakcija]
( 
	[idPro]              integer  NULL ,
	[idT]                integer  IDENTITY  NOT NULL ,
	[idPor]              integer  NULL ,
	[idK]                integer  NULL ,
	[sistem]             decimal(10,3)  NULL ,
	[datum]              datetime  NULL ,
	[novac]              decimal(10,3)  NULL 
)
go

ALTER TABLE [Artikal]
	ADD CONSTRAINT [XPKArtikal] PRIMARY KEY  CLUSTERED ([idA] ASC)
go

ALTER TABLE [Grad]
	ADD CONSTRAINT [XPKGrad] PRIMARY KEY  CLUSTERED ([idG] ASC)
go

ALTER TABLE [Kupac]
	ADD CONSTRAINT [XPKKupac] PRIMARY KEY  CLUSTERED ([idK] ASC)
go

ALTER TABLE [Linija]
	ADD CONSTRAINT [XPKLinija] PRIMARY KEY  CLUSTERED ([idL] ASC)
go

ALTER TABLE [Porudzbina]
	ADD CONSTRAINT [XPKPorudzbina] PRIMARY KEY  CLUSTERED ([idPor] ASC)
go

ALTER TABLE [Prodavnica]
	ADD CONSTRAINT [XPKProdavnica] PRIMARY KEY  CLUSTERED ([idPro] ASC)
go

ALTER TABLE [Stavka]
	ADD CONSTRAINT [XPKStavka] PRIMARY KEY  CLUSTERED ([idS] ASC)
go

ALTER TABLE [Transakcija]
	ADD CONSTRAINT [XPKTransakcija] PRIMARY KEY  CLUSTERED ([idT] ASC)
go


ALTER TABLE [Artikal]
	ADD CONSTRAINT [R_6] FOREIGN KEY ([idPro]) REFERENCES [Prodavnica]([idPro])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go


ALTER TABLE [Kupac]
	ADD CONSTRAINT [R_4] FOREIGN KEY ([idG]) REFERENCES [Grad]([idG])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go


ALTER TABLE [Linija]
	ADD CONSTRAINT [R_17] FOREIGN KEY ([idG1]) REFERENCES [Grad]([idG])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go

ALTER TABLE [Linija]
	ADD CONSTRAINT [R_18] FOREIGN KEY ([idG2]) REFERENCES [Grad]([idG])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Porudzbina]
	ADD CONSTRAINT [R_16] FOREIGN KEY ([idK]) REFERENCES [Kupac]([idK])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go

ALTER TABLE [Porudzbina]
	ADD CONSTRAINT [R_21] FOREIGN KEY ([idGNajblizi]) REFERENCES [Grad]([idG])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Porudzbina]
	ADD CONSTRAINT [R_22] FOREIGN KEY ([idG]) REFERENCES [Grad]([idG])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Prodavnica]
	ADD CONSTRAINT [R_12] FOREIGN KEY ([idG]) REFERENCES [Grad]([idG])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go


ALTER TABLE [Stavka]
	ADD CONSTRAINT [R_19] FOREIGN KEY ([idA]) REFERENCES [Artikal]([idA])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go

ALTER TABLE [Stavka]
	ADD CONSTRAINT [R_20] FOREIGN KEY ([idPor]) REFERENCES [Porudzbina]([idPor])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Transakcija]
	ADD CONSTRAINT [R_10] FOREIGN KEY ([idPro]) REFERENCES [Prodavnica]([idPro])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go

ALTER TABLE [Transakcija]
	ADD CONSTRAINT [R_9] FOREIGN KEY ([idPor]) REFERENCES [Porudzbina]([idPor])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Transakcija]
	ADD CONSTRAINT [R_5] FOREIGN KEY ([idK]) REFERENCES [Kupac]([idK])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

CREATE PROCEDURE SP_FINAL_PRICE 
	 @idOrder int
AS
BEGIN
	declare @kursor1 cursor
	declare @sumWithoutDiscount decimal(10,3)
	declare @sumWithDiscount decimal(10,3)
	declare @idItem int
	declare @amountItem int
	declare @priceArtical int
	declare @discountForShop int
	declare @date date
	declare @idK int
	declare @buyerPreviousTransactionsAmount decimal(10,3)

	select @date = p.datumPoslato, @idK = p.idK
	from Porudzbina p
	where p.idPor = @idOrder

	set @kursor1 = cursor for
		select s.idS, s.kolicina, a.cena, p.popust
		from Stavka s join Artikal a on (s.idA = a.idA)
		join Prodavnica p on (a.idPro = p.idPro)
		where s.idPor = @idOrder

	open @kursor1

	fetch next from @kursor1
	into @idItem, @amountItem, @priceArtical, @discountForShop

	set @sumWithDiscount = 0
	set @sumWithoutDiscount = 0

	--calculate price with and without a discount
	while @@FETCH_STATUS = 0
	begin
		declare @disc float
		set @disc = (100.0 - @discountForShop * 1.0)/100.0

		set @sumWithDiscount = @sumWithDiscount + @amountItem * @priceArtical * @disc
		set @sumWithoutDiscount = @sumWithoutDiscount + @amountItem * @priceArtical

		fetch next from @kursor1
		into @idItem, @amountItem, @priceArtical, @discountForShop
	end

	--check if he has bonus discount (amount of transactions > 10000)
	set @buyerPreviousTransactionsAmount = COALESCE(
    (SELECT SUM(novac) FROM Transakcija WHERE idK = @idK and DATEDIFF(day, datum, getDate()) >= -30),0)
	


	if(@buyerPreviousTransactionsAmount >= 10000.0) begin				
		set @sumWithoutDiscount = @sumWithoutDiscount * 0.98		
		set @sumWithDiscount = @sumWithDiscount * 0.98

		update Porudzbina
		set cenaUkupno = @sumWithoutDiscount,
		cenaSaPopustomUkupno = @sumWithDiscount
		where idPor = @idOrder

		--create transaction for buyer
		insert into Transakcija(idPor, idK, datum, novac, sistem) values(@idOrder, @idK ,@date, @sumWithDiscount, @sumWithDiscount * 0.03)
	end

	else begin
		--update amount with and without a discount
		update Porudzbina
		set cenaUkupno = @sumWithoutDiscount,
		cenaSaPopustomUkupno = @sumWithDiscount
		where idPor = @idOrder

		--create transaction for buyer
		insert into Transakcija(idPor, idK, datum, novac, sistem) values(@idOrder, @idK ,@date, @sumWithDiscount, @sumWithDiscount * 0.05)
	end

	--subtract money from user for transaction
	update Kupac
	set novac = novac - @sumWithDiscount
	where idK = @idK

	
	close @kursor1
	deallocate @kursor1
END
GO

CREATE TRIGGER TR_TRANSFER_MONEY_TO_SHOPS 
   ON Porudzbina 
   AFTER UPDATE
AS 
BEGIN
	declare @kursor1 cursor
	declare @priceBuyerPaid decimal(10,3)
	declare @buyerId int
	declare @orderId int
	declare @shopId int
	declare @sumToAddToShop decimal(10,3)
	declare @date date

	if update(stanje) AND EXISTS(select * from inserted where stanje = 'arrived') begin
		select @orderId = idPor, @buyerId = idK, @priceBuyerPaid = cenaSaPopustomUkupno, @date = datumStigloDoKupca
		from inserted 
		where stanje = 'arrived'

		set @kursor1 = cursor for
		select idPro
		from Prodavnica

		open @kursor1

		fetch next from @kursor1
		into @shopId
	
		while @@FETCH_STATUS = 0
		begin
			set @sumToAddToShop = 0
			select @sumToAddToShop = coalesce(sum(discountedPrice),0)
			from (
				select cena*s.kolicina * (100 - popust) / 100 AS discountedPrice
				from Stavka s join Artikal a on s.idA = a.idA
				join Prodavnica p on a.idPro = p.idPro
				where a.idPro = @shopId and s.idPor = @orderId and popust<>0
                       
				union
                      
				select cena*s.kolicina AS discountedPrice
				from Stavka s join Artikal a on s.idA = a.idA
				join Prodavnica p on a.idPro = p.idPro
				where a.idPro = @shopId and s.idPor = @orderId and popust=0 
			) as temporaryTable
			set @sumToAddToShop = @sumToAddToShop * 0.95

			-- update money for shops
			update Prodavnica
			set profit = profit + @sumToAddToShop
			where idPro = @shopId

			-- insert transaction
			insert into Transakcija (datum, idPor, idPro, novac) values(@date, @orderId, @shopId, @sumToAddToShop)

			fetch next from @kursor1
			into @shopId
		end

		close @kursor1
		deallocate @kursor1
	end
END
GO
