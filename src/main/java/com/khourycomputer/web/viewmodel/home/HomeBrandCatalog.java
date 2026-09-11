package com.khourycomputer.web.viewmodel.home;

import java.util.List;

public final class HomeBrandCatalog {

    private static final List<HomeBrandViewModel> BRANDS =
            List.of(
                    brand(
                            "HP",
                            "/images/brand/hp.png"
                    ),
                    brand(
                            "Dell",
                            "/images/brand/dell.png"
                    ),
                    brand(
                            "Lexmark",
                            "/images/brand/lexmark.png"
                    ),
                    brand(
                            "Canon",
                            "/images/brand/canon.png"
                    ),
                    brand(
                            "Epson",
                            "/images/brand/epson.png"
                    ),
                    brand(
                            "Verico",
                            "/images/brand/verico.png"
                    ),
                    brand(
                            "Silicon Power",
                            "/images/brand/silicon-power.png"
                    ),
                    brand(
                            "ADATA",
                            "/images/brand/adata.png"
                    ),
                    brand(
                            "SanDisk",
                            "/images/brand/sandisk.png"
                    ),
                    brand(
                            "Kingston",
                            "/images/brand/kingston.png"
                    ),
                    brand(
                            "WD",
                            "/images/brand/wd.png"
                    ),
                    brand(
                            "Toshiba",
                            "/images/brand/toshiba.png"
                    ),
                    brand(
                            "Microsoft",
                            "/images/brand/microsoft.png"
                    ),
                    brand(
                            "SONY",
                            "/images/brand/sony.png"
                    ),
                    brand(
                            "Logitech",
                            "/images/brand/logitech.png"
                    ),
                    brand(
                            "Fantech",
                            "/images/brand/fantech.png"
                    ),
                    brand(
                            "TP-Link",
                            "/images/brand/tp-link.png"
                    ),
                    brand(
                            "LP-Link",
                            "/images/brand/lp-link.png"
                    ),
                    brand(
                            "Tenda",
                            "/images/brand/tenda.png"
                    ),
                    brand(
                            "UBIQUITI",
                            "/images/brand/ubiquiti.png"
                    ),
                    brand(
                            "EDIMAX",
                            "/images/brand/edimax.png"
                    ),
                    brand(
                            "MSI",
                            "/images/brand/msi.png"
                    ),
                    brand(
                            "Kaspersky",
                            "/images/brand/kaspersky.png"
                    ),
                    brand(
                            "Lenovo",
                            "/images/brand/lenovo.png"
                    ),
                    brand(
                            "JBL",
                            "/images/brand/jbl.png"
                    ),
                    brand(
                            "Nikon",
                            "/images/brand/nikon.png"
                    ),
                    brand(
                            "Vtech",
                            "/images/brand/vtech.png"
                    ),
                    brand(
                            "Panasonic",
                            "/images/brand/panasonic.png"
                    ),
                    brand(
                            "Apple",
                            "/images/brand/apple.png"
                    ),
                    brand(
                            "LG",
                            "/images/brand/lg.png"
                    ),
                    brand(
                            "Asus",
                            "/images/brand/asus.png"
                    ),
                    brand(
                            "Acer",
                            "/images/brand/acer.png"
                    ),
                    brand(
                            "Samsung",
                            "/images/brand/samsung.png"
                    ),
                    brand(
                            "Hama",
                            "/images/brand/hama.png"
                    ),
                    brand(
                            "GP",
                            "/images/brand/gp.png"
                    ),
                    brand(
                            "Cudy",
                            "/images/brand/cudy.png"
                    ),
                    brand(
                            "MERCUSYS",
                            "/images/brand/mercusys.png"
                    )
            );

    private HomeBrandCatalog() {
    }

    public static List<HomeBrandViewModel> getBrands() {
        return BRANDS;
    }

    private static HomeBrandViewModel brand(
            String name,
            String logoUrl
    ) {
        return new HomeBrandViewModel(
                name,
                logoUrl
        );
    }
}