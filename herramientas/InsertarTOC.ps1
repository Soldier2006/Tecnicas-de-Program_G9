param([Parameter(Mandatory=$true)][string]$Path)
$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.IO.Compression
$w='http://schemas.openxmlformats.org/wordprocessingml/2006/main'
$xmlNs='http://www.w3.org/XML/1998/namespace'
$f=[IO.File]::Open($Path,'Open','ReadWrite')
$z=New-Object IO.Compression.ZipArchive($f,'Update')
try {
  function E($name){return $doc.CreateElement('w',$name,$w)}
  function A($node,$name,$value){[void]$node.SetAttribute($name,$w,[string]$value)}
  function Leer($ruta){$e=$z.GetEntry($ruta);$r=New-Object IO.StreamReader($e.Open());try{[xml]$x=$r.ReadToEnd();return $x}finally{$r.Dispose()}}
  function Guardar($ruta,$x){$old=$z.GetEntry($ruta);$old.Delete();$new=$z.CreateEntry($ruta);$s=$new.Open();try{$x.Save($s)}finally{$s.Dispose()}}
  $doc=Leer 'word/document.xml';$n=New-Object Xml.XmlNamespaceManager($doc.NameTable);$n.AddNamespace('w',$w);$body=$doc.SelectSingleNode('//w:body',$n)
  if($body.SelectSingleNode('.//w:instrText[contains(.,"TOC")]', $n)){Write-Output 'La tabla ya existe.';return}
  $target=$null
  foreach($p in $body.SelectNodes('w:p',$n)){if((($p.SelectNodes('.//w:t',$n)|ForEach-Object{$_.InnerText}) -join '').Trim() -eq '1. Resumen del proyecto'){$target=$p;break}}
  if(!$target){throw 'No se encontró el título 1. Resumen del proyecto.'}
  $title=E 'p';$r=E 'r';$rp=E 'rPr';$fonts=E 'rFonts';$fonts.SetAttribute('ascii','Times New Roman');$fonts.SetAttribute('hAnsi','Times New Roman');[void]$rp.AppendChild($fonts);$sz=E 'sz';A $sz 'val' '28';[void]$rp.AppendChild($sz);[void]$rp.AppendChild((E 'b'));[void]$r.AppendChild($rp);$t=E 't';$t.InnerText='Tabla de contenido';[void]$r.AppendChild($t);[void]$title.AppendChild($r)
  $field=E 'p';$r=E 'r';$fc=E 'fldChar';A $fc 'fldCharType' 'begin';A $fc 'dirty' 'true';[void]$r.AppendChild($fc);[void]$field.AppendChild($r);$r=E 'r';$it=E 'instrText';[void]$it.SetAttribute('space',$xmlNs,'preserve');$it.InnerText=' TOC \o "1-3" \h \z \u ';[void]$r.AppendChild($it);[void]$field.AppendChild($r);$r=E 'r';$fc=E 'fldChar';A $fc 'fldCharType' 'separate';[void]$r.AppendChild($fc);[void]$field.AppendChild($r);$r=E 'r';$t=E 't';$t.InnerText='Actualizar tabla de contenido en Word';[void]$r.AppendChild($t);[void]$field.AppendChild($r);$r=E 'r';$fc=E 'fldChar';A $fc 'fldCharType' 'end';[void]$r.AppendChild($fc);[void]$field.AppendChild($r)
  $break=E 'p';$r=E 'r';$br=E 'br';A $br 'type' 'page';[void]$r.AppendChild($br);[void]$break.AppendChild($r)
  [void]$body.InsertBefore($title,$target);[void]$body.InsertBefore($field,$target);[void]$body.InsertBefore($break,$target)
  Guardar 'word/document.xml' $doc
  $settings=Leer 'word/settings.xml';$sn=New-Object Xml.XmlNamespaceManager($settings.NameTable);$sn.AddNamespace('w',$w);$root=$settings.SelectSingleNode('//w:settings',$sn);$uf=$root.SelectSingleNode('w:updateFields',$sn);if(!$uf){$uf=$settings.CreateElement('w','updateFields',$w);[void]$root.AppendChild($uf)};[void]$uf.SetAttribute('val',$w,'true');Guardar 'word/settings.xml' $settings
  Write-Output 'Tabla de contenido automática insertada.'
} finally {$z.Dispose();$f.Dispose()}
