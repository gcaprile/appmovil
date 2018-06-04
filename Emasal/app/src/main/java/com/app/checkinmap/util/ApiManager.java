package com.app.checkinmap.util;

import android.app.Activity;
import android.content.Context;

import com.app.checkinmap.model.CheckPointLocation;
import com.app.checkinmap.model.Route;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Created by ASUS-PC on 02/10/2017.
 */

public class ApiManager {

    public interface OnObjectListener{
        void onObject(boolean success, JSONObject jsonObject,String errorMessage);
    }

    public interface OnArrayObjectListener{
        void onArrayObject(boolean success, JSONArray jsonArray, String errorMessage);
    }

    private static ApiManager Instance;

    public static ApiManager getInstance(){

        if(Instance==null){
            Instance = new ApiManager();
        }
        return Instance;
    }

    /**
     * This method help us to get a single JSONObject
     */
    public void getJSONObject(final Context context, String sql, final OnObjectListener listener ) {

        RestRequest restRequest = null;
        try {
            restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(context), sql);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        if (Utility.getRestClient() != null) {
            Utility.getRestClient().sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
                @Override
                public void onSuccess(RestRequest request, final RestResponse result) {
                    result.consumeQuietly(); // consume before going back to main thread
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                listener.onObject(true, result.asJSONObject(), null);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                listener.onObject(false, null, e.getMessage());
                            } catch (IOException e) {
                                e.printStackTrace();
                                listener.onObject(false, null, e.getMessage());
                            }
                        }
                    });
                }

                @Override
                public void onError(final Exception exception) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (exception != null) {
                                listener.onObject(false, null, exception.toString());
                            } else {
                                listener.onObject(false, null, null);
                            }
                        }
                    });
                }
            });
        }
    }

    /**
     * This method help us to make the upsert
     * in sales force
     */
    public void makeRouteUpsert(final Context context,Route route,final OnObjectListener listener){

        HashMap<String,Object> dataSend = new HashMap<>();
        dataSend.put("Name",route.getName());
        dataSend.put("Hora_Inicio__c",route.getStartDateSalesForceDate());
        dataSend.put("Hora_Fin__c",route.getEndDateSalesForceDate());
        dataSend.put("Kilometraje__c",route.getMileage());
        dataSend.put("Kilometraje_B__c",route.getMileageB());
        //dataSend.put("Kilometraje_C__c",route.getMileageC());
        dataSend.put("Kilometraje_C__c",0);
        dataSend.put("User__c",route.getUserId());
        dataSend.put("Coordenadas_Inicio__Latitude__s", route.getStartLatitude());
        dataSend.put("Coordenadas_Inicio__Longitude__s", route.getStartLongitude());
        dataSend.put("Coordenadas_Fin__Latitude__s", route.getEndLatitude());
        dataSend.put("Coordenadas_Fin__Longitude__s", route.getEndLongitude());
        dataSend.put("KM_Manual_Inicio__c", route.getStartOdometer());
        dataSend.put("KM_Manual_Fin__c", route.getEndOdometer());

        if(Utility.getUserRole() == Utility.Roles.TECHNICAL){
            dataSend.put("RecordTypeId","0126A000000l355QAA");
        }else{
            if(Utility.getUserRole() == Utility.Roles.SELLER){
                dataSend.put("RecordTypeId","0126A000000l34lQAA");
            }
        }

        RestRequest restRequest = null;
        try {
            restRequest = RestRequest.getRequestForUpsert(ApiVersionStrings.getVersionNumber(context), "Ruta__c",
                    "Id", null, dataSend);

            if (Utility.getRestClient() != null) {
                Utility.getRestClient().sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
                    @Override
                    public void onSuccess(RestRequest request, final RestResponse result) {
                        result.consumeQuietly(); // consume before going back to main thread
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    listener.onObject(true, result.asJSONObject(), null);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    listener.onObject(false, null, e.getMessage());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    listener.onObject(false, null, e.getMessage());
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(final Exception exception) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onObject(false, null, exception.toString());
                            }
                        });
                    }
                });

            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * This method help us to make the upsert
     * in sales force
     */
    public void makeVisitUpsert(final Context context,String routeId, CheckPointLocation visit, final OnObjectListener listener){

        HashMap<String,Object> dataSend = new HashMap<>();
        dataSend.put("Cliente_Potencial_Visitado__c",visit.getLeadId());
        dataSend.put("Contacto_Orden_de_Trabajo__c",visit.getWorkOrderContactId());
        dataSend.put("Contacto_Visitado__c",visit.getAccountContactId());
        dataSend.put("Direccion__c",visit.getAddressId());
        dataSend.put("Hora_Inicio__c",visit.getCheckInDateSalesForceDate());
        dataSend.put("Hora_Fin__c",visit.getCheckOutDateSalesForceDate());
        dataSend.put("Horas_de_Visita__c",visit.getVisitTimeNumber());
        dataSend.put("Horas_Tiempo_de_Viaje__c",visit.getTravelTimeNumber());
        dataSend.put("Orden_de_Trabajo__c",visit.getWorkOrderId());
        dataSend.put("Razon_Visita__c",visit.getVisitType());
        dataSend.put("RecordTypeId",visit.getRecordType());
        dataSend.put("Resumen_Visita__c",visit.getDescription());
        dataSend.put("Ruta__c",routeId);
        dataSend.put("Tecnico__c",visit.getTechnicalId());
        dataSend.put("Tecnico_Principal__c",visit.isMainTechnical());
        dataSend.put("Name",visit.getName());
        dataSend.put("Aprobada__c", true);
        dataSend.put("KM_Manual__c", visit.getOdometer());
        if(visit.getRecordType().compareTo("0126A000000l3CzQAI")==0){
            dataSend.put("Detalle_Direccion_Candidato__c",visit.getAddress());
        }

        RestRequest restRequest = null;
        try {
            restRequest = RestRequest.getRequestForUpsert(ApiVersionStrings.getVersionNumber(context), "Visita__c",
                    "Id", null, dataSend);

            if (Utility.getRestClient() != null) {
                Utility.getRestClient().sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
                    @Override
                    public void onSuccess(RestRequest request, final RestResponse result) {
                        result.consumeQuietly(); // consume before going back to main thread
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    listener.onObject(true, result.asJSONObject(), null);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    listener.onObject(false, null, e.getMessage());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    listener.onObject(false, null, e.getMessage());
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(final Exception exception) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onObject(false, null, exception.toString());
                            }
                        });
                    }
                });

            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * This method help us to make the upsert
     * in sales force
     */
    public void updateWorkOrderStatus(final Context context,String workOrderId, String status,final OnObjectListener listener){

        HashMap<String,Object> dataSend = new HashMap<>();
        dataSend.put("Status",status);

        RestRequest restRequest = null;
        try {
            restRequest = RestRequest.getRequestForUpdate(ApiVersionStrings.getVersionNumber(context), "WorkOrder",
                    workOrderId, dataSend);

            if (Utility.getRestClient() != null) {
                Utility.getRestClient().sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
                    @Override
                    public void onSuccess(RestRequest request, final RestResponse result) {
                        result.consumeQuietly(); // consume before going back to main thread
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result.isSuccess()) {
                                    listener.onObject(true, null, null);
                                } else {
                                    listener.onObject(false, null, null);
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(final Exception exception) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onObject(false, null, exception.toString());
                            }
                        });
                    }
                });
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method help us to make the upsert
     * in sales force
     * Update start time from a Work Order
     */
    public void updateWorkOrderStartTime(final Context context,String workOrderId, String startTime,final OnObjectListener listener){

        HashMap<String,Object> dataSend = new HashMap<>();
        dataSend.put("Hora_Inicial__c", startTime);

        RestRequest restRequest = null;
        try {
            restRequest = RestRequest.getRequestForUpdate(ApiVersionStrings.getVersionNumber(context), "WorkOrder",
                workOrderId, dataSend);

            if (Utility.getRestClient() != null) {
                Utility.getRestClient().sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
                    @Override
                    public void onSuccess(RestRequest request, final RestResponse result) {
                        result.consumeQuietly(); // consume before going back to main thread
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result.isSuccess()) {
                                    listener.onObject(true, null, null);
                                } else {
                                    listener.onObject(false, null, null);
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(final Exception exception) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onObject(false, null, exception.toString());
                            }
                        });
                    }
                });
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method help us to make the upsert
     * in sales force
     * Update final hour from a Work Order
     */
    public void updateWorkOrderFinalHour(final Context context,String workOrderId, String finalHour,final OnObjectListener listener){

        HashMap<String,Object> dataSend = new HashMap<>();
        dataSend.put("Hora_Final__c", finalHour);

        RestRequest restRequest = null;
        try {
            restRequest = RestRequest.getRequestForUpdate(ApiVersionStrings.getVersionNumber(context), "WorkOrder",
                workOrderId, dataSend);

            if (Utility.getRestClient() != null) {
                Utility.getRestClient().sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
                    @Override
                    public void onSuccess(RestRequest request, final RestResponse result) {
                        result.consumeQuietly(); // consume before going back to main thread
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result.isSuccess()) {
                                    listener.onObject(true, null, null);
                                } else {
                                    listener.onObject(false, null, null);
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(final Exception exception) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onObject(false, null, exception.toString());
                            }
                        });
                    }
                });
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method help us to make the upsert
     * in sales force
     */
    public void updateWorkOrderSignature(final Context context,String workOrderId, String whoSigns, String signature, String justification,final OnObjectListener listener){

        HashMap<String,Object> dataSend = new HashMap<>();
        dataSend.put("Nombre_de_quien_recibe__c", whoSigns);
        dataSend.put("Firma_disponible__c", signature);
        dataSend.put("Justificacion_de_Firma_NO_disponible__c", justification);

        RestRequest restRequest = null;
        try {
            restRequest = RestRequest.getRequestForUpdate(ApiVersionStrings.getVersionNumber(context), "WorkOrder",
                workOrderId, dataSend);

            if (Utility.getRestClient() != null) {
                Utility.getRestClient().sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
                    @Override
                    public void onSuccess(RestRequest request, final RestResponse result) {
                        result.consumeQuietly(); // consume before going back to main thread
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result.isSuccess()) {
                                    listener.onObject(true, null, null);
                                } else {
                                    listener.onObject(false, null, null);
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(final Exception exception) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onObject(false, null, exception.toString());
                            }
                        });
                    }
                });
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * This method help us to make the update
     * in technical work order data
     */
    public void updateThecnicalWorkOrderData(final Context context, CheckPointLocation visit,final OnObjectListener listener){

        HashMap<String,Object> dataSend = new HashMap<>();
        dataSend.put("Horas_de_Trabajo__c",visit.getVisitTimeNumber());
        dataSend.put("Horas_Tiempo_de_Viaje__c",visit.getTravelTimeNumber());
        dataSend.put("Hora_Inicio_Trabajo__c",visit.getCheckInDateSalesForceDate());
        dataSend.put("Hora_Fin_Trabajo__c",visit.getCheckOutDateSalesForceDate());

        RestRequest restRequest = null;
        try {
            restRequest = RestRequest.getRequestForUpdate(ApiVersionStrings.getVersionNumber(context), "Tecnicos_por_Orden_de_Trabajo__c",
                    visit.getWorkOderXTechnicalId(), dataSend);

            if (Utility.getRestClient() != null) {
                Utility.getRestClient().sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
                    @Override
                    public void onSuccess(RestRequest request, final RestResponse result) {
                        result.consumeQuietly(); // consume before going back to main thread
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result.isSuccess()) {
                                    listener.onObject(true, null, null);
                                } else {
                                    listener.onObject(false, null, null);
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(final Exception exception) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onObject(false, null, exception.toString());
                            }
                        });
                    }
                });
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method help us to make the update
     * in the address object when the new coordinates
     */
    public void updateAddressCoordinates(final Context context,String objectId,String objectName, HashMap<String,Object> dataSend ,final OnObjectListener listener){
        RestRequest restRequest = null;
        try {
            restRequest = RestRequest.getRequestForUpdate(ApiVersionStrings.getVersionNumber(context), objectName,
                    objectId, dataSend);

            if (Utility.getRestClient() != null) {
                Utility.getRestClient().sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
                    @Override
                    public void onSuccess(RestRequest request, final RestResponse result) {
                        result.consumeQuietly(); // consume before going back to main thread
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result.isSuccess()) {
                                    listener.onObject(true, null, null);
                                } else {
                                    listener.onObject(false, null, result.toString());
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(final Exception exception) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onObject(false, null, exception.toString());
                            }
                        });
                    }
                });
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method help us to send the sing image
     * to sales force object
     */
    public void sendSingToSalesForce(final Context context, String workOrderId, String fileImagePath,String fileName,final OnObjectListener listener){

        HashMap<String,Object> dataSend = new HashMap<>();
        dataSend.put("Name",fileName);
        dataSend.put("ParentId",workOrderId);
        dataSend.put("Body",ImageHelper.getBase64FromImage(fileImagePath));

        RestRequest restRequest = null;
        try {
            //Attachment
            restRequest = RestRequest.getRequestForCreate(ApiVersionStrings.getVersionNumber(context), "Attachment",dataSend);

            if (Utility.getRestClient() != null) {
                Utility.getRestClient().sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
                    @Override
                    public void onSuccess(RestRequest request, final RestResponse result) {
                        result.consumeQuietly(); // consume before going back to main thread
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result.isSuccess()) {
                                    try {
                                        listener.onObject(true, result.asJSONObject(), null);
                                    } catch (JSONException e) {
                                        listener.onObject(false, null, e.getMessage());
                                    } catch (IOException e) {
                                        listener.onObject(false, null, e.getMessage());
                                    }
                                } else {
                                    listener.onObject(false, null, null);
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(final Exception exception) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onObject(false, null, exception.toString());
                            }
                        });
                    }
                });
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method help us to make the upsert
     * in sales force to save user backup
     */
    public void makeBackUpUpsert(final Context context,final OnObjectListener listener){

        HashMap<String,Object> dataSend = new HashMap<>();
        dataSend.put("User__c",Utility.getRestClient().getClientInfo().userId);

        RestRequest restRequest = null;
        try {
            Utility.logLargeString("Numero de API"+ApiVersionStrings.getVersionNumber(context));
            restRequest = RestRequest.getRequestForUpsert(ApiVersionStrings.getVersionNumber(context), "Reporte_de_Errores_App_Movil__c",
                    "Id", null, dataSend);

            if (Utility.getRestClient() != null) {
                Utility.getRestClient().sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
                    @Override
                    public void onSuccess(RestRequest request, final RestResponse result) {
                        result.consumeQuietly(); // consume before going back to main thread
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    listener.onObject(true, result.asJSONObject(), null);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    listener.onObject(false, null, e.getMessage());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    listener.onObject(false, null, e.getMessage());
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(final Exception exception) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onObject(false, null, exception.toString());
                            }
                        });
                    }
                });

            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method help us to send the backup file
     * to sales force object
     */
    public void sendBackUpFile(final Context context, String backUpId, File file, final OnObjectListener listener){

        HashMap<String,Object> dataSend = new HashMap<>();
        dataSend.put("Name",file.getName());
        dataSend.put("ParentId",backUpId);
        dataSend.put("Body",Utility.encodeFileToBase64(file));

        RestRequest restRequest = null;
        try {
            //Attachment
            restRequest = RestRequest.getRequestForCreate(ApiVersionStrings.getVersionNumber(context), "Attachment",dataSend);

            if (Utility.getRestClient() != null) {
                Utility.getRestClient().sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
                    @Override
                    public void onSuccess(RestRequest request, final RestResponse result) {
                        result.consumeQuietly(); // consume before going back to main thread
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result.isSuccess()) {
                                    try {
                                        listener.onObject(true, result.asJSONObject(), null);
                                    } catch (JSONException e) {
                                        listener.onObject(false, null, e.getMessage());
                                    } catch (IOException e) {
                                        listener.onObject(false, null, e.getMessage());
                                    }
                                } else {
                                    listener.onObject(false, null, null);
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(final Exception exception) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onObject(false, null, exception.toString());
                            }
                        });
                    }
                });
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


