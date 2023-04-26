package ru.ev.TinkoffService.service;


import org.springframework.web.bind.annotation.RestController;
import ru.ev.TinkoffService.dto.FigiesDto;
import ru.ev.TinkoffService.dto.StocksDto;
import ru.ev.TinkoffService.dto.StocksPricesDto;
import ru.ev.TinkoffService.dto.TickersDto;
import ru.ev.TinkoffService.model.Stock;

public interface StockService {
    Stock getStockByTicker(String ticker);

    StocksDto getStocksByTickers(TickersDto tickersDto);

    StocksPricesDto getPricesStocksByFigies(FigiesDto figiesDto);
}
