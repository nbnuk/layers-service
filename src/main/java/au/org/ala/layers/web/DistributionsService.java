/**************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 *
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/
package au.org.ala.layers.web;

import au.org.ala.layers.dao.DistributionDAO;
import au.org.ala.layers.dao.ObjectDAO;
import au.org.ala.layers.dto.AttributionDTO;
import au.org.ala.layers.dto.Distribution;
import au.org.ala.layers.dto.Facet;
import au.org.ala.layers.dto.MapDTO;
import au.org.ala.layers.util.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * @author Adam
 */
@Controller
public class DistributionsService {

    private final String WS_DISTRIBUTIONS = "/distributions";
    private final String WS_DISTRIBUTIONS_COUNTS = "/distributions/counts";
    private final String WS_DISTRIBUTIONS_RADIUS = "/distributions/radius";
    private final String WS_DISTRIBUTIONS_RADIUS_COUNTS = "/distributions/radius/counts";
    private final String WS_DISTRIBUTION_ID = "/distribution/{spcode}";
    private final String WS_DISTRIBUTION_LSID = "/distribution/lsid/**";
    private final String WS_DISTRIBUTION_OVERVIEWMAP = "/distribution/map/**";
    private final String WS_DISTRIBUTION_OVERVIEWMAP_PNG = "/distribution/map/png/{geomIdx}";
    private final String WS_DISTRIBUTION_OVERVIEWMAP_SEED = "/distribution/map/seed";
    private final String WS_DISTRIBUTION_OUTLIERS = "/distribution/outliers/**";
    private final String WS_ATTRIBUTION_CACHE = "/attribution/clearCache";

    private final String WS_DISTRIBUTION_OVERVIEWMAP_PNG_LSID = "/distribution/map/lsid/**";
    private final String WS_DISTRIBUTION_OVERVIEWMAP_PNG_SPCODE = "/distribution/map/spcode/{spcode:.+}";
    private final String WS_DISTRIBUTION_OVERVIEWMAP_PNG_NAME = "/distribution/map/name/{scientificName:.+}";
    private final String WS_DISTRIBUTION_LSIDS = "/distribution/lsids/**";
    private final String WS_DISTRIBUTION_OVERVIEWMAPS = "/distribution/maps/**";
    

    /**
     * Log4j instance
     */
    protected Logger logger = Logger.getLogger(this.getClass());

    @Resource(name = "distributionDao")
    private DistributionDAO distributionDao;

    @Resource(name = "objectDao")
    private ObjectDAO objectDao;

    private Properties userProperties = (new UserProperties()).getProperties();

    /*
     * list distribution table records, GET
     */
    @RequestMapping(value = WS_DISTRIBUTIONS, method = {RequestMethod.GET, RequestMethod.POST})
    public
    @ResponseBody
    List<Distribution> listDistributionsGet(@RequestParam(value = "wkt", required = false, defaultValue = "") String wkt,
                                            @RequestParam(value = "min_depth", required = false, defaultValue = "-1") Double min_depth, @RequestParam(value = "max_depth", required = false, defaultValue = "-1") Double max_depth,
                                            @RequestParam(value = "lsids", required = false, defaultValue = "") String lsids, @RequestParam(value = "geom_idx", required = false, defaultValue = "-1") Integer geom_idx,
                                            @RequestParam(value = "fid", required = false) String fid, @RequestParam(value = "objectName", required = false) String objectName,
                                            @RequestParam(value = "pelagic", required = false) Boolean pelagic, @RequestParam(value = "coastal", required = false) Boolean coastal,
                                            @RequestParam(value = "estuarine", required = false) Boolean estuarine, @RequestParam(value = "desmersal", required = false) Boolean desmersal,
                                            @RequestParam(value = "groupName", required = false) String groupName, @RequestParam(value = "family", required = false) String[] families,
                                            @RequestParam(value = "familyLsid", required = false) String[] familyLsids, @RequestParam(value = "genus", required = false) String[] genera,
                                            @RequestParam(value = "genusLsid", required = false) String[] generaLsids, @RequestParam(value = "dataResourceUid", required = false) String[] dataResourceUids,
                                            @RequestParam(value = "endemic", required = false) Boolean endemic,
                                            HttpServletResponse response) {

        if (StringUtils.isEmpty(wkt) && fid != null && objectName != null) {
            List<au.org.ala.layers.dto.Objects> objects = objectDao.getObjectByFidAndName(fid, objectName);
            // TODO this might be better served with a stored proc
            // so that the polygon isnt passed from DB to java
            wkt = objects.get(0).getGeometry();
            if (wkt == null) {
                logger.info("Unmatched geometry for name: " + objectName + " and layer " + fid);
                try {
                    response.sendError(400);
                } catch (Exception e) {
                    logger.error("Error sending response code 400 to client.");
                }
                return null;
            }
        }

        List<Distribution> list = null;
                
        if (wkt.startsWith("GEOMETRYCOLLECTION")) {
            List<String> collectionParts = SpatialConversionUtils.getGeometryCollectionParts(wkt);

            Set<Distribution> distributionsSet = new HashSet<Distribution>();

            for (String part : collectionParts) {
                distributionsSet.addAll(distributionDao.queryDistributions(part, min_depth, max_depth, pelagic, coastal, estuarine, desmersal, groupName, geom_idx, lsids, families, familyLsids,
                        genera, generaLsids, Distribution.EXPERT_DISTRIBUTION, dataResourceUids, endemic));
            }

            list = new ArrayList<Distribution>(distributionsSet);
        } else {
            list = distributionDao.queryDistributions(wkt, min_depth, max_depth, pelagic, coastal, estuarine, desmersal, groupName, geom_idx, lsids, families, familyLsids, genera, generaLsids,
                    Distribution.EXPERT_DISTRIBUTION, dataResourceUids, endemic);
        }

        addImageUrls(list);

        return list;
    }

    /*
     * list distribution table records, GET
     */
    @RequestMapping(value = WS_DISTRIBUTIONS_COUNTS, method = {RequestMethod.GET, RequestMethod.POST})
    public
    @ResponseBody
    List<Facet> listDistributionsGetCounts(@RequestParam(value = "wkt", required = false, defaultValue = "") String wkt,
                                           @RequestParam(value = "min_depth", required = false, defaultValue = "-1") Double min_depth, @RequestParam(value = "max_depth", required = false, defaultValue = "-1") Double max_depth,
                                           @RequestParam(value = "lsids", required = false, defaultValue = "") String lsids, @RequestParam(value = "geom_idx", required = false, defaultValue = "-1") Integer geom_idx,
                                           @RequestParam(value = "fid", required = false) String fid, @RequestParam(value = "objectName", required = false) String objectName,
                                           @RequestParam(value = "pelagic", required = false) Boolean pelagic, @RequestParam(value = "coastal", required = false) Boolean coastal,
                                           @RequestParam(value = "estuarine", required = false) Boolean estuarine, @RequestParam(value = "desmersal", required = false) Boolean desmersal,
                                           @RequestParam(value = "groupName", required = false) String groupName, @RequestParam(value = "family", required = false) String[] families,
                                           @RequestParam(value = "familyLsid", required = false) String[] familyLsids, @RequestParam(value = "genus", required = false) String[] genera,
                                           @RequestParam(value = "genusLsid", required = false) String[] generaLsids, @RequestParam(value = "dataResourceUid", required = false) String[] dataResourceUids,
                                           @RequestParam(value = "endemic", required = false) Boolean endemic,
                                           HttpServletResponse response) {

        if (StringUtils.isEmpty(wkt) && fid != null && objectName != null) {
            List<au.org.ala.layers.dto.Objects> objects = objectDao.getObjectByFidAndName(fid, objectName);
            // TODO this might be better served with a stored proc
            // so that the polygon isn't passed from DB to java
            wkt = objects.get(0).getGeometry();
            if (wkt == null) {
                logger.info("Unmatched geometry for name: " + objectName + " and layer " + fid);
                try {
                    response.sendError(400);
                } catch (Exception e) {
                    logger.error("Error sending response code 400 to client.");
                }
                return null;
            }
        }
        return distributionDao.queryDistributionsFamilyCounts(wkt, min_depth, max_depth, pelagic, coastal, estuarine, desmersal, groupName, geom_idx, lsids, families, familyLsids, genera,
                generaLsids, Distribution.EXPERT_DISTRIBUTION, dataResourceUids, endemic);
    }

    @RequestMapping(value = WS_DISTRIBUTIONS_RADIUS, method = {RequestMethod.GET, RequestMethod.POST})
    public
    @ResponseBody
    List<Distribution> listDistributionsForRadiusGet(@RequestParam(value = "min_depth", required = false, defaultValue = "-1") Double min_depth,
                                                     @RequestParam(value = "max_depth", required = false, defaultValue = "-1") Double max_depth, @RequestParam(value = "lsids", required = false, defaultValue = "") String lsids,
                                                     @RequestParam(value = "geom_idx", required = false, defaultValue = "-1") Integer geom_idx, @RequestParam(value = "lon", required = true) Float longitude,
                                                     @RequestParam(value = "lat", required = true) Float latitude, @RequestParam(value = "radius", required = true) Float radius,
                                                     @RequestParam(value = "pelagic", required = false) Boolean pelagic, @RequestParam(value = "coastal", required = false) Boolean coastal,
                                                     @RequestParam(value = "estuarine", required = false) Boolean estuarine, @RequestParam(value = "desmersal", required = false) Boolean desmersal,
                                                     @RequestParam(value = "groupName", required = false) String groupName, @RequestParam(value = "family", required = false) String[] families,
                                                     @RequestParam(value = "familyLsid", required = false) String[] familyLsids, @RequestParam(value = "genus", required = false) String[] genera,
                                                     @RequestParam(value = "genusLsid", required = false) String[] generaLsids, @RequestParam(value = "dataResourceUid", required = false) String[] dataResourceUids,
                                                     @RequestParam(value = "endemic", required = false) Boolean endemic) {
        List<Distribution> list = distributionDao.queryDistributionsByRadius(longitude, latitude, radius, min_depth, max_depth, pelagic, coastal, estuarine, desmersal, groupName, geom_idx, lsids, families, familyLsids,
                genera, generaLsids, Distribution.EXPERT_DISTRIBUTION, dataResourceUids, endemic);
        addImageUrls(list);
        return list;
    }

    @RequestMapping(value = WS_DISTRIBUTIONS_RADIUS_COUNTS, method = {RequestMethod.GET, RequestMethod.POST})
    public
    @ResponseBody
    List<Facet> listDistributionCountsForRadiusGet(@RequestParam(value = "min_depth", required = false, defaultValue = "-1") Double min_depth,
                                                   @RequestParam(value = "max_depth", required = false, defaultValue = "-1") Double max_depth, @RequestParam(value = "lsids", required = false, defaultValue = "") String lsids,
                                                   @RequestParam(value = "geom_idx", required = false, defaultValue = "-1") Integer geom_idx, @RequestParam(value = "lon", required = true) Float longitude,
                                                   @RequestParam(value = "lat", required = true) Float latitude, @RequestParam(value = "radius", required = true) Float radius,
                                                   @RequestParam(value = "pelagic", required = false) Boolean pelagic, @RequestParam(value = "coastal", required = false) Boolean coastal,
                                                   @RequestParam(value = "estuarine", required = false) Boolean estuarine, @RequestParam(value = "desmersal", required = false) Boolean desmersal,
                                                   @RequestParam(value = "groupName", required = false) String groupName, @RequestParam(value = "family", required = false) String[] families,
                                                   @RequestParam(value = "familyLsid", required = false) String[] familyLsids, @RequestParam(value = "genus", required = false) String[] genera,
                                                   @RequestParam(value = "genusLsid", required = false) String[] generaLsids, @RequestParam(value = "dataResourceUid", required = false) String[] dataResourceUids,
                                                   @RequestParam(value = "endemic", required = false) Boolean endemic) {
        return distributionDao.queryDistributionsByRadiusFamilyCounts(longitude, latitude, radius, min_depth, max_depth, pelagic, coastal, estuarine, desmersal, groupName, geom_idx, lsids, families,
                familyLsids, genera, generaLsids, Distribution.EXPERT_DISTRIBUTION, dataResourceUids, endemic);
    }

    /*
     * get distribution by id
     */
    @RequestMapping(value = WS_DISTRIBUTION_ID, method = RequestMethod.GET)
    public
    @ResponseBody
    Distribution getDistribution(@PathVariable Long spcode,
                                 @RequestParam(value = "nowkt", required = false, defaultValue = "false") Boolean noWkt) {
        Distribution d = distributionDao.getDistributionBySpcode(spcode, Distribution.EXPERT_DISTRIBUTION, noWkt);
        addImageUrl(d);

        return d;
    }

    /*
     * get distribution by id
     */
    @RequestMapping(value = WS_DISTRIBUTION_LSID, method = RequestMethod.GET)
    public
    @ResponseBody
    Distribution getDistribution(@RequestParam(value = "nowkt", required = false, defaultValue = "false") Boolean noWkt,
                                 HttpServletRequest request, HttpServletResponse response) throws Exception {
        String lsid = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        lsid = filterLsid(lsid);
        List<Distribution> distributions = distributionDao.getDistributionByLSID(new String[]{lsid}, Distribution.EXPERT_DISTRIBUTION, noWkt);
        if (distributions != null && !distributions.isEmpty()) {
            Distribution d = distributions.get(0);
            addImageUrl(d);
            return d;
        } else {
            response.sendError(404);
            return null;
        }
    }

    /*
     * get distribution by id
     */
    @RequestMapping(value = WS_DISTRIBUTION_LSIDS, method = RequestMethod.GET)
    public
    @ResponseBody
    List<Distribution> getDistributions(@RequestParam(value = "nowkt", required = false, defaultValue = "false") Boolean noWkt,
                                        HttpServletRequest request, HttpServletResponse response) throws Exception {
        String lsid = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        lsid = filterLsid(lsid);
        List<Distribution> distributions = distributionDao.getDistributionByLSID(new String[]{lsid}, Distribution.EXPERT_DISTRIBUTION, noWkt);
        if (distributions != null && !distributions.isEmpty()) {
            addImageUrls(distributions);
            return distributions;
        } else {
            response.sendError(404);
            return null;
        }
    }

    /*
     * get distribution by id
     */
    @RequestMapping(value = WS_DISTRIBUTION_OVERVIEWMAP, method = RequestMethod.GET)
    public
    @ResponseBody
    MapDTO getDistributionOverviewMap(@RequestParam(value = "height", required = false, defaultValue = "504") Integer height,
                                      @RequestParam(value = "width", required = false, defaultValue = "512") Integer width,
                                      HttpServletRequest request, HttpServletResponse response) throws Exception {
        String lsid = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        lsid = filterLsid(lsid);
        Distribution distribution = distributionDao.findDistributionByLSIDOrName(lsid, Distribution.EXPERT_DISTRIBUTION);
        if (distribution != null) {
            MapDTO m = new MapDTO();
            m.setDataResourceUID(distribution.getData_resource_uid());
            m.setUrl(userProperties.getProperty("layers_service_url") + "/distribution/map/png/" + distribution.getGeom_idx());
            // set the attribution info
            AttributionDTO dto = AttributionCache.getCache().getAttributionFor(distribution.getData_resource_uid());
            m.setAvailable(true);
            m.setDataResourceName(dto.getName());
            m.setLicenseType(dto.getLicenseType());
            m.setLicenseVersion(dto.getLicenseVersion());
            m.setRights(dto.getRights());
            m.setDataResourceUrl(dto.getWebsiteUrl());
            m.setMetadataUrl(dto.getAlaPublicUrl());
            return m;
        } else {
            return new MapDTO();
        }
    }

    /*
     * get distribution by id
     */
    @RequestMapping(value = WS_DISTRIBUTION_OVERVIEWMAPS, method = RequestMethod.GET)
    public
    @ResponseBody
    List<MapDTO> getDistributionOverviewMaps(@RequestParam(value = "height", required = false, defaultValue = "504") Integer height,
                                             @RequestParam(value = "width", required = false, defaultValue = "512") Integer width,
                                             HttpServletRequest request, HttpServletResponse response) throws Exception {
        String lsid = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        lsid = filterLsid(lsid);
        List<Distribution> distributions = distributionDao.findDistributionsByLSIDOrName(lsid, Distribution.EXPERT_DISTRIBUTION);
        List<MapDTO> maps = new ArrayList<MapDTO>();
        if (distributions != null && distributions.size() > 0) {
            for (Distribution distribution : distributions) {
                MapDTO m = new MapDTO();
                m.setDataResourceUID(distribution.getData_resource_uid());
                m.setUrl(userProperties.getProperty("layers_service_url") + "/distribution/map/png/" + distribution.getGeom_idx());
                // set the attribution info
                AttributionDTO dto = AttributionCache.getCache().getAttributionFor(distribution.getData_resource_uid());
                m.setAvailable(true);
                m.setDataResourceName(dto.getName());
                m.setLicenseType(dto.getLicenseType());
                m.setLicenseVersion(dto.getLicenseVersion());
                m.setRights(dto.getRights());
                m.setDataResourceUrl(dto.getWebsiteUrl());
                m.setMetadataUrl(dto.getAlaPublicUrl());

                maps.add(m);
            }
            return maps;
        } else {
            response.sendError(404);
            return null;
        }
    }
    
    /*
     * get distribution by id
     */
    @RequestMapping(value = WS_ATTRIBUTION_CACHE, method = RequestMethod.GET)
    public void clearAttributionCache() throws Exception {
        AttributionCache.getCache().clear();
    }

    @RequestMapping(value = WS_DISTRIBUTION_OVERVIEWMAP_SEED, method = RequestMethod.GET)
    public void getDistributionOverviewMapSeed() throws Exception {

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    List<Distribution> distributions = distributionDao.queryDistributions(null, -1, -1, null, null, null, null, null, null, null, null, null, null, null,
                            Distribution.EXPERT_DISTRIBUTION, null, null);

                    for (Distribution d : distributions) {
                        MapCache.getMapCache().cacheMap(d.getGeom_idx().toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    /*
     * get distribution by id
     */
    @RequestMapping(value = WS_DISTRIBUTION_OVERVIEWMAP_PNG, method = RequestMethod.GET)
    public void getDistributionOverviewMapPng(@PathVariable String geomIdx, HttpServletResponse response) throws Exception {
        InputStream input = MapCache.getMapCache().getCachedMap(geomIdx);
        OutputStream out = response.getOutputStream();
        byte[] buff = new byte[1024];
        int read = 0;
        while ((read = input.read(buff)) > 0) {
            out.write(buff, 0, read);
        }
        out.flush();
        out.close();
        input.close();
    }

    /**
     * For a given set of points and an lsid, identify the points which do not
     * fall within the expert distribution associated with the lsid.
     *
     * @param lsid       the lsid associated with the expert distribution
     * @param pointsJson the points to test in JSON format. This must be a map whose
     *                   keys are point ids (strings - typically these will be
     *                   occurrence record ids). The values are maps containing the
     *                   point's decimal latitude (with key "decimalLatitude") and
     *                   decimal longitude (with key "decimalLongitude"). The decimal
     *                   latitude and longitude values must be numbers.
     * @param response   the http response
     * @return A map containing the distance outside the expert distribution for
     * each point which falls outside the area defined by the
     * distribution. Keys are point ids, values are the distances
     * @throws Exception
     */
    @RequestMapping(value = WS_DISTRIBUTION_OUTLIERS, method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Double> getDistributionOutliers(@RequestParam(value = "pointsJson", required = true) String pointsJson,
                                                HttpServletRequest request, HttpServletResponse response) throws Exception {
        String lsid = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        lsid = filterLsid(lsid);
        JSONParser parser = new JSONParser();
        try {
            Map<String, Map<String, Double>> pointsMap = (Map<String, Map<String, Double>>) parser.parse(pointsJson);
            try {
                Map<String, Double> outlierDistances = distributionDao.identifyOutlierPointsForDistribution(lsid, pointsMap, Distribution.EXPERT_DISTRIBUTION);
                return outlierDistances;
            } catch (IllegalArgumentException ex) {
                response.sendError(400, "No expert distribution for species associated with supplied lsid");
                return null;
            }
        } catch (ParseException ex) {
            response.sendError(400, "Invalid JSON for point information");
            return null;
        } catch (ClassCastException ex) {
            response.sendError(400, "Invalid format for point information");
            return null;
        }
    }

    /*
     * get distribution image
     */
    @RequestMapping(value = WS_DISTRIBUTION_OVERVIEWMAP_PNG_LSID, method = RequestMethod.GET)
    public void getDistributionOverviewMapPngLsid(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String lsid = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        lsid = filterLsid(lsid);
        getImage(response, lsid, null, null);
    }

    /*
     * get distribution image
     */
    @RequestMapping(value = WS_DISTRIBUTION_OVERVIEWMAP_PNG_SPCODE, method = RequestMethod.GET)
    public void getDistributionOverviewMapPngSpcode(@PathVariable Long spcode, HttpServletResponse response) throws Exception {
        getImage(response, null, spcode, null);
    }

    /*
     * get distribution image
     */
    @RequestMapping(value = WS_DISTRIBUTION_OVERVIEWMAP_PNG_NAME, method = RequestMethod.GET)
    public void getDistributionOverviewMapPngName(@PathVariable String scientificName, HttpServletResponse response) throws Exception {
        getImage(response, null, null, scientificName);
    }

    /**
     * returns writes one image to the HttpServletResponse for lsid, spcode or scientificName match
     * *
     *
     * @param response
     * @param lsid
     * @param spcode
     * @param scientificName
     * @throws Exception
     */
    private void getImage(HttpServletResponse response, String lsid, Long spcode, String scientificName) throws Exception {
        Long geomIdx = null;

        try {
            if (spcode != null) {
                geomIdx = distributionDao.getDistributionBySpcode(spcode, Distribution.EXPERT_DISTRIBUTION, true).getGeom_idx();
            } else if (lsid != null) {
                geomIdx = distributionDao.getDistributionByLSID(new String[]{lsid}, Distribution.EXPERT_DISTRIBUTION, true).get(0).getGeom_idx();
            } else if (scientificName != null) {
                geomIdx = distributionDao.findDistributionByLSIDOrName(scientificName, Distribution.EXPERT_DISTRIBUTION).getGeom_idx();
            }
        } catch (Exception e) {

        }

        if (geomIdx != null) {
            InputStream input = MapCache.getMapCache().getCachedMap(String.valueOf(geomIdx));

            OutputStream out = response.getOutputStream();
            byte[] buff = new byte[1024];
            int read;
            while ((read = input.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
            out.flush();
            out.close();
            input.close();
        } else {
            response.sendError(404);
        }
    }

    void addImageUrls(List<Distribution> list) {
        for (Distribution d : list) {
            addImageUrl(d);
        }
    }

    void addImageUrl(Distribution d) {
        d.setImageUrl(userProperties.getProperty("layers_service_url") + "/distribution/map/png/" + d.getGeom_idx());
    }

    String filterLsid(String lsid) {
        if (lsid != null) {
            String lower = lsid.toLowerCase();
            if (lower.endsWith(".png") || lower.endsWith(".jpg")) {
                lsid = lsid.substring(0, lsid.length() - 4);
            } else if (lower.endsWith(".json")) {
                lsid = lsid.substring(0, lsid.length() - 5);
            }
        }

        return lsid;
    }
}
