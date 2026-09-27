/**
 * CUFA Workshop Router 2
 * Paste as a NEW Apps Script project and deploy as Web App.
 * Uses the existing CUFA spreadsheet; does not modify Router 1.
 */
const CUFA_SHEET_ID='1g3igxCPyY3gdGhv5vsZoDPdyWxLEYO0XqutAjo3XCxI';
const T={clients:'Klientet',vehicles:'Automjetet',services:'Serviset',obd:'OBD_Historiku'};
function doGet(e){return out({ok:true,service:'CUFA Workshop Router 2',version:'1.0'});}
function doPost(e){try{const p=JSON.parse(e.postData.contents||'{}');switch(p.action){
case 'client.create':return createClient(p);case 'vehicle.create':return createVehicle(p);case 'service.create':return createService(p);
case 'vehicle.history':return vehicleHistory(p);case 'vehicle.search':return vehicleSearch(p);case 'obd.save':return saveObd(p);
default:return out({ok:false,error:'UNKNOWN_ACTION'});}}catch(err){return out({ok:false,error:String(err.message||err)});}}
function sh(n){return SpreadsheetApp.openById(CUFA_SHEET_ID).getSheetByName(n);}
function id(prefix){return prefix+'-'+Utilities.getUuid().slice(0,8).toUpperCase();}
function now(){return Utilities.formatDate(new Date(),'Europe/Belgrade','yyyy-MM-dd HH:mm:ss');}
function out(x){return ContentService.createTextOutput(JSON.stringify(x)).setMimeType(ContentService.MimeType.JSON);}
function append(tab,row){sh(tab).appendRow(row);return row[0];}
function createClient(p){const x=id('K');append(T.clients,[x,p.name||'',p.surname||'',p.phone||'',p.whatsapp||'',p.email||'',p.address||'',now()]);return out({ok:true,id:x});}
function createVehicle(p){const x=id('A');append(T.vehicles,[x,p.clientId||'',p.plate||'',p.vin||'',p.make||'',p.model||'',p.year||'',p.engine||'',p.fuel||'',p.mileage||'',p.color||'',p.notes||'',now()]);return out({ok:true,id:x});}
function createService(p){const x=id('S');append(T.services,[x,p.vehicleId||'',p.clientId||'',p.date||now(),p.mileage||'',p.type||'',p.oil||'',p.filters||'',p.parts||'',p.work||'',p.faults||'',p.dtc||'',p.mechanic||'Fidan',p.nextDate||'',p.nextKm||'',p.notes||'']);return out({ok:true,id:x});}
function saveObd(p){const x=id('O');append(T.obd,[x,p.vehicleId||'',now(),p.device||'OBDLink MX+',p.vin||'',p.dtc||'',p.rpm||'',p.speed||'',p.coolant||'',p.voltage||'',JSON.stringify(p.live||{}),p.ai||'',p.notes||'']);return out({ok:true,id:x});}
function rows(tab){const a=sh(tab).getDataRange().getDisplayValues();const h=a.shift()||[];return a.map(r=>Object.fromEntries(h.map((k,i)=>[k,r[i]])));}
function vehicleSearch(p){const q=String(p.q||'').toLowerCase();return out({ok:true,items:rows(T.vehicles).filter(v=>Object.values(v).some(x=>String(x).toLowerCase().includes(q))).slice(0,50)});}
function vehicleHistory(p){const v=String(p.vehicleId||'');return out({ok:true,vehicle:rows(T.vehicles).find(x=>x['Automjet ID']===v)||null,services:rows(T.services).filter(x=>x['Automjet ID']===v),obd:rows(T.obd).filter(x=>x['Automjet ID']===v)});}
