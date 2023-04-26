package ru.ev.TinkoffService.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.ev.TinkoffService.dto.StocksDto;
import ru.ev.TinkoffService.dto.TickersDto;
import ru.ev.TinkoffService.model.Stock;
import ru.ev.TinkoffService.service.StockService;

@RestController
@RequiredArgsConstructor
public class StockController {
    private final StockService stockService;
    @GetMapping("/stocks/{ticker}")
    public Stock getStock(@PathVariable String ticker){
        return stockService.getStockByTicker(ticker);
    }
    @PostMapping("/stocks/getStocksByTickers")
    public StocksDto getStocksByTickers(@RequestBody TickersDto tickersDto){
        return stockService.getStocksByTickers(tickersDto);
    }
}
