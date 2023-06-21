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
