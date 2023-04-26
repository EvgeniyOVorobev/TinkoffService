package ru.ev.TinkoffService.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import ru.ev.TinkoffService.dto.*;
import ru.ev.TinkoffService.exeption.StockNotFoundException;

import ru.ev.TinkoffService.model.Currency;
import ru.ev.TinkoffService.model.Stock;
import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrumentList;
import ru.tinkoff.invest.openapi.model.rest.Orderbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TinkoffStockService implements StockService{

    private final OpenApi openApi;
    @Async
    public CompletableFuture<MarketInstrumentList> getMarketInstrumentTicker(String ticker){
        var context= openApi.getMarketContext();
       return context.searchMarketInstrumentsByTicker(ticker);
    }
    @Override
    public Stock getStockByTicker(String ticker) {
       var cf=getMarketInstrumentTicker(ticker);
       var list= cf.join().getInstruments();
       if(list.isEmpty()){
           throw new StockNotFoundException(String.format ("Stock %s not found", ticker));
       }
       var item=list.get(0);
       return new Stock(
               item.getTicker(),
               item.getFigi(),
               item.getName(),
               item.getType().getValue(),
               Currency.valueOf(item.getCurrency().getValue()),
                       "Tinkoff ");
    }
@Override
    public StocksDto getStocksByTickers(TickersDto tickers){
        List<CompletableFuture<MarketInstrumentList>> marketInstrument=new ArrayList<>();
        tickers.getTickers().forEach(ticker->marketInstrument.add(getMarketInstrumentTicker(ticker)));
        List<Stock> stocks=marketInstrument.stream()
                .map(CompletableFuture::join)
                .map(mi->{
                    if(!mi.getInstruments().isEmpty()){
                        return mi.getInstruments().get(0);
                    }
                    return  null;
                })
                .filter(el -> Objects.nonNull(el))
                .map(mi -> new Stock(
                        mi.getTicker(),
                        mi.getFigi(),
                        mi.getName(),
                        mi.getType().getValue(),
                        Currency.valueOf(mi.getCurrency().getValue()),
                        "TINKOFF"))
                .collect(Collectors.toList());

        return new StocksDto(stocks);
    }
    @Async
    public CompletableFuture<Optional<Orderbook>> getOrderBookByFigi(String figi) {
        var orderBook = openApi.getMarketContext().getMarketOrderbook(figi, 0);
        log.info("Getting price {} from Tinkoff", figi);
        return orderBook;
    }

    public StocksPricesDto getPricesStocksByFigies(FigiesDto figiesDto) {
        long start = System.currentTimeMillis();
        List<CompletableFuture<Optional<Orderbook>>> orderBooks = new ArrayList<>();
        figiesDto.getFigies().forEach(figi -> orderBooks.add(getOrderBookByFigi(figi)));
        List<StockPrice> prices =  orderBooks.stream()
                .map(CompletableFuture::join)
                .map(oo -> oo.orElseThrow(() -> new StockNotFoundException("Stock not found.")))
                .map(orderBook -> new StockPrice(
                        orderBook.getFigi(),
                        orderBook.getLastPrice().doubleValue())).collect(Collectors.toList());

        log.info("Time getting prices - {}", System.currentTimeMillis() - start);
        return new StocksPricesDto(prices);
    }
}
