package com.khourycomputer.web.viewmodel.home;

import java.util.List;

public final class HomeBrandCatalog {

    private static final List<HomeBrandViewModel> BRANDS =
            List.of(
                    brand(
                            "HP",
                            "/images/brand/hp.webp"
                    ),
                    brand(
                            "Dell",
                            "/images/brand/dell.webp"
                    ),
                    brand(
                            "Lexmark",
                            "/images/brand/lexmark.webp"
                    ),
                    brand(
                            "Canon",
                            "/images/brand/canon.webp"
                    ),
                    brand(
                            "Epson",
                            "/images/brand/epson.webp"
                    ),
                    brand(
                            "Verico",
                            "/images/brand/verico.webp"
                    ),
                    brand(
                            "Silicon Power",
                            "/images/brand/silicon-power.webp"
                    ),
                    brand(
                            "ADATA",
                            "/images/brand/adata.webp"
                    ),
                    brand(
                            "SanDisk",
                            "/images/brand/sandisk.webp"
                    ),
                    brand(
                            "Kingston",
                            "/images/brand/kingston.webp"
                    ),
                    brand(
                            "WD",
                            "/images/brand/wd.webp"
                    ),
                    brand(
                            "Toshiba",
                            "/images/brand/toshiba.webp"
                    ),
                    brand(
                            "Microsoft",
                            "/images/brand/microsoft.webp"
                    ),
                    brand(
                            "SONY",
                            "/images/brand/sony.webp"
                    ),
                    brand(
                            "Logitech",
                            "/images/brand/logitech.webp"
                    ),
                    brand(
                            "Fantech",
                            "/images/brand/fantech.webp"
                    ),
                    brand(
                            "TP-Link",
                            "/images/brand/tp-link.webp"
                    ),
                    brand(
                            "LP-Link",
                            "/images/brand/lp-link.webp"
                    ),
                    brand(
                            "Tenda",
                            "/images/brand/tenda.webp"
                    ),
                    brand(
                            "UBIQUITI",
                            "/images/brand/ubiquiti.webp"
                    ),
                    brand(
                            "EDIMAX",
                            "/images/brand/edimax.webp"
                    ),
                    brand(
                            "MSI",
                            "/images/brand/msi.webp"
                    ),
                    brand(
                            "Kaspersky",
                            "/images/brand/kaspersky.webp"
                    ),
                    brand(
                            "Lenovo",
                            "/images/brand/lenovo.webp"
                    ),
                    brand(
                            "JBL",
                            "/images/brand/jbl.webp"
                    ),
                    brand(
                            "Nikon",
                            "/images/brand/nikon.webp"
                    ),
                    brand(
                            "Vtech",
                            "/images/brand/vtech.webp"
                    ),
                    brand(
                            "Panasonic",
                            "/images/brand/panasonic.webp"
                    ),
                    brand(
                            "Apple",
                            "/images/brand/apple.webp"
                    ),
                    brand(
                            "LG",
                            "/images/brand/lg.webp"
                    ),
                    brand(
                            "Asus",
                            "/images/brand/asus.webp"
                    ),
                    brand(
                            "Acer",
                            "/images/brand/acer.webp"
                    ),
                    brand(
                            "Samsung",
                            "/images/brand/samsung.webp"
                    ),
                    brand(
                            "Hama",
                            "/images/brand/hama.webp"
                    ),
                    brand(
                            "GP",
                            "/images/brand/gp.webp"
                    ),
                    brand(
                            "Cudy",
                            "/images/brand/cudy.webp"
                    ),
                    brand(
                            "MERCUSYS",
                            "/images/brand/mercusys.webp"
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